package com.arunwizz.crawlersystem.application;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Writer;
import java.io.FileWriter;
import java.io.*;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.helper.W3CDom;

import com.arunwizz.crawlersystem.application.ds.tree.GeneralizedNode;
import com.arunwizz.crawlersystem.application.ds.tree.Node;
import com.arunwizz.crawlersystem.application.ds.tree.Tree;
import com.arunwizz.crawlersystem.application.ds.tree.TreeUtil;
import com.arunwizz.crawlersystem.application.pageparser.HTMLParser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HTMLPageCrawler {

    private static Log logger = LogFactory.getLog(HTMLPageCrawler.class);

    public static void main(String argv[]) throws Exception {
        HTMLParser pd = null;// HTMLParserFactory.getParser(HTMLParserFactory.HTMLCleanerParser);//SEEMS GOOD
        // HTMLParser pd =
        // HTMLParserFactory.getParser(HTMLParserFactory.XHTMLDOMParser);

        // String[][] urlList = new String[][] {
        // { "https", "www.microchipdirect.com",
        // "/Chart.aspx?branchId=30044&mid=14&treeid=3101" },
        // { "https", "www.schukat.com",
        // "/schukat/schukat_cms_en.nsf/index/CMSDF15D356B046D53BC1256D550038A9E0?OpenDocument&wg=U1232&refDoc=CMS322921A477B31844C125707B0034EB15"
        // },
        // { "https", "my.mouser.com/", "/new/microchip" }, };

        String[] urlList = new String[] {
                "https://www.microchipdirect.com/Chart.aspx?branchId=30044&mid=14&treeid=3101",
                "https://www.schukat.com/schukat/schukat_cms_en.nsf/index/CMSDF15D356B046D53BC1256D550038A9E0?OpenDocument&wg=U1232&refDoc=CMS322921A477B31844C125707B0034EB15",
                "https://www.arrow.com/en/manufacturers/microchip-technology/microcontrollers-and-processors/microcontrollers",
                "https://www.digikey.com/products/en/integrated-circuits-ics/embedded-fpgas-field-programmable-gate-array-with-microcontrollers/767",
                "http://www.newark.com/w/c/semiconductors-ics/microcontrollers-mcu/16-32-bit-microcontrollers-mcu-arm?brand=microchip&range=inc-new",
                "https://www.nxp.com/products/processors-and-microcontrollers/arm-based-processors-and-mcus/lpc-cortex-m-mcus/lpc800-series-cortex-m0-plus-mcus:MC_71785",
                "https://global.epson.com/products_and_drivers/semicon/products/micro_controller/16bit/#ac01" };

        String[] urlReferenceList = new String[] {
                "https://www.microchipdirect.com/Chart.aspx?branchId=8197&mid=14&treeid=8",
                "https://www.schukat.com/schukat/schukat_cms_en.nsf/index/CMSF52334E11D475306C125707B00358DA1?OpenDocument",
                "https://www.arrow.com/en/manufacturers/microchip-technology/microcontrollers-and-processors/secure-microcontrollers-and-tpm",
                "https://www.digikey.com/products/en/integrated-circuits-ics/interface-analog-switches-special-purpose/780",
                "https://www.newark.com/c/semiconductors-ics/microcontrollers-mcu/8-bit-microcontrollers-mcu",
                "https://www.nxp.com/products/processors-and-microcontrollers/arm-based-processors-and-mcus/lpc-cortex-m-mcus/lpc54000-series-cortex-m4-mcus:MC_1414576688124",
                "https://global.epson.com/products_and_drivers/semicon/products/micro_controller/8bit/" };

        Integer index = 5;
        boolean useFixedThreshold = false;
        float t = 0.3f;
        float thresholdStep = 0.1f;

        String url_str = urlList[index];
        String url_refer_str = urlReferenceList[index];

        // URI uri = new URI(urlList[index][0], urlList[index][1], urlList[index][2],
        // null);

        // String url_str = uri.toASCIIString();
        // url_str = url_str.replaceFirst("%3[f|F]", "?");// some servers couldn't
        // understand encoded ? as query separator.
        // logger.info("Encoded Url: " + url_str);

        // TODO:
        // InputStream pageStream = PageLoader.loadPage(url);
        // InputStream pageStream =
        // PageLoader.loadPage("http://www.chinadaily.com.cn/index.html");

        // InputStream pageStream =
        // PageLoader.loadPage("file://testPages/canon.html");//FAILED
        // InputStream pageStream =
        // PageLoader.loadPage("file://testPages/kodak.html");//WILL PASS
        // InputStream pageStream =
        // PageLoader.loadPage("file://testPages/kodak1.html");//FAILED
        // InputStream pageStream =
        // PageLoader.loadPage("file://testPages/nikon.html");//FAILED
        // InputStream pageStream =
        // PageLoader.loadPage("file://testPages/polaroid.html");//failed
        // InputStream pageStream =
        // PageLoader.loadPage("file://testPages/sanyo1.html");//failed
        // InputStream pageStream =
        // PageLoader.loadPage("file://testPages/sony.html");//failed - not strict xhtml

        // Parsing first page into DOM

        Date d1 = new Date();
        logger.info("getting first page at " + d1.getTime());

        org.jsoup.nodes.Document pageDomTreeJsoup = Jsoup.connect(url_str).userAgent("Mozilla").timeout(30000).get();

        logger.info("got first page in " + (new Date().getTime() - d1.getTime()) / 1000.0 + " seconds");
        d1 = new Date();

        logger.info("parsing first page at " + d1.getTime());

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document pageDomTree = w3cDom.fromJsoup(pageDomTreeJsoup);

        logger.info("parsed first page in " + (new Date().getTime() - d1.getTime()) / 1000.0 + " seconds");

        TreeUtil tutil = new TreeUtil();
        Tree<String> pageTree = tutil.getTreeFromDOM(pageDomTree);

        // Parsing second page into DOM

        d1 = new Date();
        logger.info("getting second page at " + d1.getTime());

        pageDomTreeJsoup = Jsoup.connect(url_refer_str).userAgent("Mozilla").timeout(30000).get();

        logger.info("got second page in " + (new Date().getTime() - d1.getTime()) / 1000.0 + " seconds");
        d1 = new Date();

        logger.info("parsing second page at " + d1.getTime());

        org.w3c.dom.Document pageDomReferTree = w3cDom.fromJsoup(pageDomTreeJsoup);

        logger.info("parsed second page in " + (new Date().getTime() - d1.getTime()) / 1000.0 + " seconds");

        Tree<String> pageReferTree = tutil.getTreeFromDOM(pageDomReferTree);

        // -----------------------------------------------------------------------------------------------

        // Finding unique nodes between two DOM trees

        // Map<Integer, Boolean> uniqueNodeMap = new HashMap<Integer, Boolean>();
        List<Node<String>> uniqueNodeList = new ArrayList<Node<String>>();
        Map<Integer, Node<String>> uniqueNodeMap = new HashMap<Integer, Node<String>>();

        for (Node<String> child : pageTree.traverse(Tree.PRE_ORDER)) {
            for (Node<String> childRefer : pageReferTree.traverse(Tree.PRE_ORDER)) {
                if (child.isSame(childRefer)) {
                    child.setDuplicatedCount(child.getDuplicatedCount() + 1);
                    break;
                }
            }
        }

        for (Node<String> child : pageTree.traverse(Tree.PRE_ORDER)) {
            if (child.getDuplicatedCount() == 0) {
                if (child.getData() != null && !child.getData().equals("")) {
                    boolean hasFound = false;

                    for (Node<String> uniqueNode : uniqueNodeList) {
                        if (uniqueNode.isSame(child)) {
                            hasFound = true;
                            break;
                        }
                    }

                    if (!hasFound) {
                        uniqueNodeList.add(child);
                        uniqueNodeMap.put(child.getPreOrderPosition(), child);
                        logger.info(child.getData());
                    }

                    // uniqueNodeMap.put(child.getPreOrderPosition(), true);
                }
            }
        }

        logger.info("List size: " + uniqueNodeList.size());
        logger.info("Map size: " + uniqueNodeMap.size());

        // -----------------------------------------------------------------------------------------------

        // int uniqueCount = uniqueNodeList.size();

        // Call MDR and FINDDR to identify all data regions

        int k = 10;// maximum combination
        // float t = 0.3f;// edit distance threshold
        int oldCount = 0;
        int uniqueCount = uniqueNodeList.size();

        while (true) {
            if (useFixedThreshold != true) {
                t += thresholdStep;
            }

            tutil.mdr(pageTree.getRoot(), k);
            tutil.findDR(pageTree.getRoot(), k, t);

            List<List<GeneralizedNode<Node<String>>>> drList = tutil.getDRs(pageTree);
            // logger.info(drList.size());
            // logger.info(drList);

            for (List<GeneralizedNode<Node<String>>> dr : drList) {
                /* for each data region */
                if (dr.get(0).size() == 1) {
                    tutil.findRecord1(dr.get(0));// Case 1
                } else {
                    tutil.findRecordN(dr.get(0));// Case 2
                }
            }

            // Data Extraction from DEPTA Section 4, Page# 80.
            /*
             * Produce one rooted tag tree for each data records for each data region.
             */

            List<List<List<List<String>>>> outputFile = new ArrayList<List<List<List<String>>>>();

            List<List<List<String>>> outputTable = new ArrayList<List<List<String>>>();

            int indexTest = -1;
            for (List<GeneralizedNode<Node<String>>> dr : drList) {
                indexTest++;
                logger.info("DR i: " + indexTest);

                // logger.info(dr);

                /* for each data region */
                PriorityQueue<Tree<String>> dataRecordQueue = tutil.buildDataRecrodTree(dr);

                if (indexTest == 14) {
                    boolean flag = true;
                }

                List<Tree<String>> alignedDataRecords = tutil.partialTreeAlignment(dataRecordQueue);

                // logger.info(alignedDataRecords);
                // logger.info("-----------------------------------------");

                List<Node<String>> seedChildren = alignedDataRecords.get(0).getRoot().getChildren();

                List<List<String>> outputRows = new ArrayList<List<String>>();

                int indexTest2 = -1;
                for (Tree<String> tree : alignedDataRecords) {
                    indexTest2++;
                    logger.info("Tree i: " + indexTest2);

                    // for (Node<String> child : tree.traverse(Tree.PRE_ORDER)) {
                    // System.out.println(child.getData());
                    // }

                    boolean hasUniqueNode = false;

                    for (Node<String> child : tree.traverse(Tree.PRE_ORDER)) {
                        if (child.getDuplicatedCount() == 0) {
                            hasUniqueNode = true;
                            break;
                        }
                    }
                    
                    if (hasUniqueNode) {
                        if (tree.getRoot().getChildren().size() > 0) {
                            Node<String> currChild = tree.getRoot().getChildren().get(0);
                            int currIndex = 0;

                            List<String> outputColumns = new ArrayList<String>();

                            for (int i = 0; i < seedChildren.size(); i++) {
                                if (currChild == null) {
                                    outputColumns.add("");
                                } else if (currChild.isSameWithoutData(seedChildren.get(i))) {
                                    List<Node<String>> childList = new Tree<String>(currChild).traverse(Tree.PRE_ORDER);
                                    List<Node<String>> filterList = new ArrayList<Node<String>>();
                                    String temp = "";

                                    for (Node<String> subChild : childList) {
                                        if (subChild.getData() != null) {
                                            boolean hasFound = false;

                                            for (Node<String> filterChild : filterList) {
                                                if (filterChild.isSame(subChild)) {
                                                    hasFound = true;
                                                    break;
                                                }
                                            }

                                            if (!hasFound) {
                                                temp += subChild.getData() + " | ";

                                                // for (Node<String> uniqueNode : uniqueNodeList) {
                                                // if (uniqueNode.isSame(subChild) && uniqueNode.getHasMatched() ==
                                                // false) {
                                                // currCount++;
                                                // uniqueNode.setHasMatched(true);
                                                // break;
                                                // }
                                                // }

                                                if (subChild.getDuplicatedCount() == 0
                                                        && !subChild.getData().equals("")) {
                                                    if (uniqueNodeMap.containsKey(subChild.getPreOrderPosition())) {
                                                        uniqueNodeMap.get(subChild.getPreOrderPosition()).setIsMatched(true);
                                                    }                                                    
                                                }

                                                // logger.info(subChild.getPreOrderPosition());

                                                // if (subChild.getPreOrderPosition() == 990) {
                                                // boolean flag = true;
                                                // }

                                                filterList.add(subChild);
                                            }
                                        }
                                    }

                                    outputColumns.add(temp);
                                    // logger.info(temp);

                                    currIndex++;

                                    if (currIndex >= tree.getRoot().getChildren().size()) {
                                        currChild = null;
                                    } else {
                                        currChild = tree.getRoot().getChildren().get(currIndex);
                                    }
                                }
                            }

                            outputRows.add(outputColumns);
                            // logger.info("-------------------------------");
                        }
                    }
                }

                outputTable.add(outputRows);
            }

            outputFile.add(outputTable);

            int matchedCount = 0;

            for (Map.Entry<Integer, Node<String>> entry : uniqueNodeMap.entrySet()) {
                if (entry.getValue().getIsMatched() == true) {
                    matchedCount++;
                    entry.getValue().setIsMatched(false);
                }
            }

            logger.info("Current threshold: " + String.valueOf(t));
            logger.info("Matched unique counts: " + String.valueOf(matchedCount));
            logger.info("Total unique counts: " + String.valueOf(uniqueCount));
            logger.info("=================================");

            if (useFixedThreshold != true) {
                if (matchedCount == uniqueCount /*|| oldCount > matchedCount*/) {
                    logger.info("Done.");
                    break;
                } else {
                    oldCount = matchedCount;

                    try (Writer writer = new FileWriter(System.getProperty("user.dir") + "/output/"
                            + urlList[index].replaceAll("[\\\\/:*?\"<>|]", "") + ".json")) {
                        new GsonBuilder().create().toJson(outputFile, writer);
                    }
                }
            } else {
                try (Writer writer = new FileWriter(System.getProperty("user.dir") + "/output/"
                        + urlList[index].replaceAll("[\\\\/:*?\"<>|]", "") + ".json")) {
                    new GsonBuilder().create().toJson(outputFile, writer);
                }

                logger.info("Done.");
                break;
            }

            // logger.info(pageTree);

            // Tree<String> temp = pageTree.getSubTreeByPreOrder(55);
            // System.out.println(temp);

            // logger.info(currCount >= uniqueCount);

            // else {
            // for (Node<String> uniqueNode : uniqueNodeList) {
            // uniqueNode.setHasMatched(false);
            // }
            // }

            pageTree = tutil.getTreeFromDOM(pageDomTree);

            // if (t == 0.4f) {
            // break;
            // }

            // break;
        }

        /*
         * Source source = new DOMSource(pageDomTree); StringWriter stringWriter = new
         * StringWriter(); Result result = new StreamResult(stringWriter);
         * TransformerFactory factory = TransformerFactory.newInstance(); Transformer
         * transformer = factory.newTransformer(); transformer.transform(source,
         * result); logger.info(stringWriter.getBuffer().toString());
         */

        // pd.setUrl("http://www.amazon.com/s/ref=amb_link_85318851_27?ie=UTF8&node=565108&field-price=00000-39999&field-availability=-1&emi=ATVPDKIKX0DER&pf_rd_m=ATVPDKIKX0DER&pf_rd_s=center-6&pf_rd_r=143DDSYDRCRZ6D7DMYDQ&pf_rd_t=101&pf_rd_p=1288331602&pf_rd_i=565108");
        // pd.setUrl("http://www.w3.org/TR/xhtml1/");
    }
}