Fusionsidentifikation:

- java Thread_organizer.java query_coverage sorted_BLAST_archives Threadcount // Bentigt Ordner THREAD_DUMP(zeigt fertige Organismen an pro Thread) und FuDet_OUT fr Output; Bentigt alle Proteinlngen
Input: query_coverage in %; Liste aller BLAST_outputs (Alle Proteine gegen 1 Organismus), getrennt in Parts, sortiert nach Organismus; gewnschte Anzahl Threads
Output: potentielle Fusionen zusammen mit allen mglichen Komponenten
#################BEISPIEL######################(In einer Zeile Tabstoppgetrennt)
GCF_000005845.2_NP_415103.1::GCF_000734975.2_WP_022523385.1(1-222)      GCF_000734975.2_WP_035555376.1(1-226)   GCF_000734975.2_WP_035556039.1(1-221)
                                                         GCF_000734975.2_WP_035556291.1(1-224) GCF_000734975.2_WP_035561696.1(1-222)   GCF_000734975.2_WP_035561703.1(1-221)
                                                         GCF_000734975.2_WP_035561803.1(1-222)   GCF_000734975.2_WP_035562557.1(1-221) GCF_000734975.2_WP_045812220.1(1-104)
                                                         GCF_000734975.2_WP_035557059.1(2-227)   GCF_000734975.2_WP_035558598.1(2-116)   GCF_000734975.2_WP_008957133.1(3-219)
                                                         GCF_000734975.2_WP_035555963.1(3-221)   GCF_000734975.2_WP_035562415.1(3-226)   GCF_000734975.2_WP_035563777.1(3-221)   GCF_000734975.2_WP_052703714.1(135-221)
#################BEISPIEL######################

- java QQ_filter_v2_2 FuDet_OUT_Liste Threadcount // Bentigt Ordner QQfilter_OUT_v2.2 fr Output; Bentigt all_QQ_sort.blast (alle BLASTS wo Query-Organismus = Subject-Organismus);
Input: Liste der Namen in FuDet_OUT; gewnschte Anzahl Threads
Output: potentielle Fusionen, gefiltert nach Query-Query Kriterium (kein gegenseitiger BLAST Treffer der Komponenten)

- Berechne bereinstimmende Fusionen zu Henry et al. (1999) Methode und filter potentielle Fusions entsprechend (Dateien in QQfilter_OUT_v2.2)
Output: Ordner COMM_FUSIONS


Fusionspunktberechnung:

- java NoL_array_organizer QQ_filter_v2_2_OUT_Liste Threadcount // Bentigt Ordner COMM_NoLArr fr Output; Bentigt all_QQ_sort.blast
Input: potentielle Fusions nach Bestimmung der bereinstimmenden potentiellen Fusionen (in Ordner COMM_FUSIONS); gewnschte Anzahl Threads
Output: Array der Komponenten pro mglicher Schnittposition (nicht normalisiert auf Organimus); Alle Komponenten mit Partner
#################BEISPIEL######################(In einer Zeile)
GCF_000005845.2_NP_415103.1::GCF_000734975.2::[1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
                                                                                           2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
                                                                                           2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1,
                                                                                           1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                                                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                                                                                           1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0]
                                                                                           ::GCF_000734975.2_WP_045812220.1(1-104)     GCF_000734975.2_WP_052703714.1(135-221) GCF_000734975.2_WP_035558598.1(2-116)
#################BEISPIEL######################

- sortiere COMM_NoLArr Output nach Fusionsprotein (nach Ordner COMM_NoLArr_sort)

- perl GET_SUMS_ORG.pl COMM_NoLArr_sort/ORG_qcov90.QQ.comm.NoLArr_sort >SUMS_ORG/ORG_qcov90.QQ.comm.NoLArr_sort.SUM
Output: Aufsummierte auf Organismen normalisierte Liste der Komponenten pro Schnittposition
#################BEISPIEL######################(In einer Zeile)
GCF_000005845.2_NP_415103.1::1, 3, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                                                         5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                                                         5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 3,
                                                         3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                                                         5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
                                                         5, 5, 5, 4, 4, 2, 2, 1, 1, 0, 0
#################BEISPIEL######################

- sh MinMax_org_prot.sh SUMS_ORG/ORG_qcov90.QQ.comm.NoLArr_sort.SUM // Bentigt Ordner MINMAX_POS, MINMAX_NEG und WINDOW_OUT fr Output
Output: MINMAX_POS: Liste der potentiellen Fusionen fr die ein Schnittpunkt(e) berechnet werden konnte::Schnittpunkt(e)::untersttzende Organismen, untersttzende Komponenten
                MINMAX_NEG: Liste der potentiellen Fusionen fr die kein Schnittpunkt berechnet werden konnte
                WINDOW_OUT: Liste der absoluten nderungen, Schnittpunktberechnungsbereiche und Schwellenwerte fr positive Fusionspunkte:
                                        Accesion::nderung im Fenster::absolute nderung im Schnittpunktberechnungsbereich::Schnittpunktberechnungsbereich::Schwellenwert fr absolute nderung im Schnittpunktberechnungsbereich
                                        ::Schwellenwert fr Einzelnderung::Schwellenwert fr zu kleine nderung
#################BEISPIEL######################
MINMAX_POS:
GCF_000005845.2_NP_415103.1::123::5,12
WINDOW_OUT:
GCF_000005845.2_NP_415103.1::5, 4, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                                         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1,
                                                         -1, -1, -1, -2, -2, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, -3, 3, 3,
                                                         3, 4, 4, 4, 4, 4, 4, 4, 4, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                                         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -3, -3, -4, -4, -5
                                                         ::8::76-190::TH 4::sTH 0.5::zTH 0
#################BEISPIEL######################
