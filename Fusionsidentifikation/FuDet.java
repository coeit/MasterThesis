import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Constantin on 07.06.17.
 *
 */
public class FuDet implements Runnable {
    private Map<String, Map<String, String>> BLAST2 = new HashMap<String, Map<String, String>>((int) (6337982/0.6));
    private String qcov;
    private ArrayList<String> archiveList;
    int thrNr;
    public HashMap<String,Double> acc2len;
    public FuDet(String Qcov, ArrayList<String> ArchiveList, HashMap<String, Double> Acc2len, int ThrNr){
        this.qcov = Qcov;
        this.archiveList = ArchiveList;
        this.acc2len = Acc2len;
        this.thrNr = ThrNr;
    }
    public void run() {
        File progFile = new File("THREAD_DUMP/Thread_" + thrNr + "_prog.txt");
        try {
            progFile.delete();
            progFile.createNewFile();
            FileWriter writer = new FileWriter(progFile);
            String[] split;
            String[] split2;
            String GCF;
            int allcnt = 1;
            boolean sw = false;
            split = archiveList.get(0).split("vs");
            String lastGCF = split[1].substring(1,16);
            String lastname = lastGCF + "_qcov" + qcov + ".fusion";
            for (String archiveLine : archiveList){
                archiveLine = archiveLine.trim();
                split = archiveLine.split("GCF");
                split2 = split[1].split("_");
                GCF = "GCF_" + split2[1];
                String command = "tar xOf /databases/RefSeq_Genomes_Sep2016/BLAST/BLAST_archives_gzip.tar " + archiveLine + "| gunzip";
                String[] cmd = {
                        "/bin/sh",
                        "-c",
                        command
                };
                String s;
                String name = GCF + "_qcov" + qcov + ".fusion";
                if(!GCF.equals(lastGCF)){
                    detecFusion(lastname);
                    System.err.print(".");
                    writer.write(lastGCF + " finished!\n");
                    writer.flush();
                } else {
                    if(allcnt == archiveList.size()){
                        sw = true;
                    }

                }
                Process p = Runtime.getRuntime().exec(cmd);
                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(p.getInputStream()));
                while ((s = stdInput.readLine()) != null) {
                    addToFusionHash(qcov, s);
                }
                if(sw){
                    detecFusion(name);
                    System.err.print(".");
                    writer.write(GCF + " finished!\n");
                    writer.flush();
                }
                lastGCF = GCF;
                lastname = name;
                allcnt++;
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**Builds Hash out of BLAST line**/
    public void addToFusionHash(String qcovlim, String BLASTline) {
        Double qcovLim = Double.parseDouble(qcovlim) / 100;
        Map<String, String> innerBLAST2;
        String[] fields = BLASTline.split("\t");
        String[] fields_ = BLASTline.split("_");
        String componentorg = fields_[0] + "_" + fields_[1];
        Double qcov = (Double.parseDouble(fields[7]) - Double.parseDouble(fields[6]) + 1) / acc2len.get(fields[0]);
        if (Double.parseDouble(fields[10]) <= 1e-10 && qcov >= qcovLim) {
            if (BLAST2.containsKey(componentorg)) { /** EXTEND INNER HASH **/
                if (BLAST2.get(componentorg).containsKey(fields[1])) {
                    String tmp = BLAST2.get(componentorg).get(fields[1]) + "\t" + fields[0] + "(" + fields[8] + "-" + fields[9] + ")";
                    innerBLAST2 = BLAST2.get(componentorg);
                    innerBLAST2.put(fields[1], tmp);
                } else { /** NEW INNER HASH **/
                    innerBLAST2 = BLAST2.get(componentorg);
                    innerBLAST2.put(fields[1], fields[0] + "(" + fields[8] + "-" + fields[9] + ")");
                }

            } else { /** NEW OUTER HASH **/
                innerBLAST2 = new HashMap<String, String>();
                innerBLAST2.put(fields[1], fields[0] + "(" + fields[8] + "-" + fields[9] + ")");
                BLAST2.put(componentorg, innerBLAST2);

            }
        }
    }
    /**Filters  possible Fusions from the finished Hash**/
    public void detecFusion(String filename){
        File outfile = new File("FuDet_OUT/" + filename);
        try {
            outfile.delete();
            outfile.createNewFile();
            FileWriter writer = new FileWriter(outfile);
            Pattern findqcov = Pattern.compile("(GCF[^\\t:]+)\\((\\d+-\\d+)\\)");
            for (String compositeorg : BLAST2.keySet()) {
                for (String compositeprot : BLAST2.get(compositeorg).keySet()) {
                    if(BLAST2.get(compositeorg).get(compositeprot).contains("\t")){
                        String[] split = BLAST2.get(compositeorg).get(compositeprot).split("\t");
                        ArrayList<Gene> tmpComponentList = new ArrayList<Gene>();
                        for(String component : split) {
                            Matcher GeneMat = findqcov.matcher(component);
                            if(GeneMat.find()){
                                String name = GeneMat.group(1);
                                String posA_posE = GeneMat.group(2);
                                String[] pos = posA_posE.split("-");
                                Gene tmp = new Gene(name,Integer.parseInt(pos[0]), Integer.parseInt(pos[1]));
                                tmpComponentList.add(tmp);
                            }
                        }
                        Collections.sort(tmpComponentList,new Comparator<Gene>() {
                            public int compare(Gene gene1, Gene gene2) {
                                return ((Integer) gene1.getStart()).compareTo(gene2.getStart());
                            }
                        });



                        boolean overlap = checkOverlap(tmpComponentList, 10.0);
                        if(overlap){
                            writer.write(compositeprot + "::");
                            for(Gene gene : tmpComponentList){
                                writer.write(gene.getGenename() + "(" + gene.getStart() + "-" + gene.getEnd() + ")" + "\t");
                            }
                            writer.write("\n");
                        }
                    }
                }
            }
            writer.flush();
            writer.close();
            BLAST2.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkOverlap(ArrayList<Gene> GeneList, Double lim){
        for (int i = 0; i < GeneList.size()-1; i++){
            for (int j = i+1; j < GeneList.size(); j++){
                if(Genecmp(GeneList.get(i), GeneList.get(j), lim)){
                    return true;
                }
            }
        }
        return false;
    }

    public  boolean Genecmp(Gene gene1, Gene gene2, Double lim){
        Double gene1Len = acc2len.get(gene1.getGenename());
        Double gene2Len = acc2len.get(gene2.getGenename());
        if (gene1.getEnd() - gene2.getStart() + 1 < (gene1Len+gene2Len) * (lim/100)) {
            return true;
        }
        else { return false;}
    }

    public class Gene {
        String Genename;
        int start;
        int end;

        public Gene(String genename, int Start, int End) {
            this.Genename = genename;
            this.end = End;
            this.start = Start;
        }
        public int getEnd() {
            return end;
        }
        public int getStart() {
            return start;
        }
        public String getGenename() { return Genename; }
    }
}
