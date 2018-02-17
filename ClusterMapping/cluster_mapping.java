import java.io.*;
import java.util.*;

/**
 * compile with: javac cluster_mapping.java
 * run with: java cluster_mapping args[0] args[1] args[2]
 * args[0]: path to "reference cluster"
 * args[1]: path to cluster to map "reference cluster" to
 * args[2]: output Path
 * OUTPUT:  column1: index "reference"
 *          column2: proteins in "reference"
 *          column3: index "mapped"
 *          column4: proteins in "mapped"
 *          column5: overlap
 *          Sorted by best hitting cluster
 *
 *          Proteins that don't cluster in the mapped clusters will be printed to StdErr
 */
public class cluster_mapping {
    static HashMap<Integer, Integer> cl1_index_2_size = new HashMap<Integer, Integer>();

    public static void main(String[] args) {
        HashMap<String, Integer> id_cl1_2_cl1_index = fill_cluster1_Map(args[0]); /**Hash for reference cluster: ProteinID --> ClusterIndex**/
        System.out.println("id_cl1_2_cl1_index read!");
        File OUT_table = new File(args[2]);

        try {
            String sCurrentLine;
            BufferedReader br = new BufferedReader(new FileReader(args[1]));
            FileWriter writer = new FileWriter(OUT_table);
            int cluster_index = 1;
            while ((sCurrentLine = br.readLine()) != null) {
                Map<Integer, Integer> tmp = new HashMap<Integer, Integer>(); /**Hash for mapped cluster(for every cluster reinitialised) ClusterIndex of Protein in reference Cluster --> number of proteins with that Index**/
                sCurrentLine.trim();
                String[] split = sCurrentLine.split("\t");
                int err_count = 1;
                for (String s : split) {
                    if (id_cl1_2_cl1_index.containsKey(s)) {
                        if (tmp.containsKey(id_cl1_2_cl1_index.get(s))) {
                            int old = tmp.get(id_cl1_2_cl1_index.get(s));
                            tmp.put(id_cl1_2_cl1_index.get(s), old + 1);
                        } else {
                            tmp.put(id_cl1_2_cl1_index.get(s), 1);
                        }
                    } else {
                        System.err.println(s);
                        tmp.put(-1, err_count);
                        err_count++;
                    }
                }
                Map<Integer, Integer> tmp_sort = sortByValue(tmp); /**sort the Hash by highest Value (max number of proteins that map to a specific reference cluster)**/
                for (Integer cl1_index : tmp_sort.keySet()) {
                    if(cl1_index != -1){
                        writer.write(cluster_index + "\t" + split.length + "\t" + cl1_index + "\t" + cl1_index_2_size.get(cl1_index) + "\t" + tmp_sort.get(cl1_index) + "\n");
                    } else {
                        writer.write(cluster_index + "\t" + split.length + "\t" + "-" + "\t" + "-" + "\t" + tmp_sort.get(cl1_index) + "\n");
                    }

                }
                cluster_index++;
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static HashMap<String, Integer> fill_cluster1_Map(String filename) {
        HashMap<String, Integer> ret = new HashMap<String, Integer>(20000000);
        BufferedReader br;
        int cluster_index = 1;
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader(filename));
            while ((sCurrentLine = br.readLine()) != null) {
                sCurrentLine.trim();
                String[] split = sCurrentLine.split("\t");
                for (String s : split) {
                    if (ret.containsKey(s)) {
                        System.err.println(s + " -- multiple Occurance in Clusters");
                    }
                    ret.put(s, cluster_index);
                    cl1_index_2_size.put(cluster_index, split.length);
                }
                cluster_index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}