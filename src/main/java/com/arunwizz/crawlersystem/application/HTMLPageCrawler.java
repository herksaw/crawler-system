package com.arunwizz.crawlersystem.application;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;

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

public class HTMLPageCrawler {
    
    private static Log logger = LogFactory.getLog(HTMLPageCrawler.class);

    public static void main(String argv[]) throws Exception {
        HTMLParser pd = null;//HTMLParserFactory.getParser(HTMLParserFactory.HTMLCleanerParser);//SEEMS GOOD
        //HTMLParser pd = HTMLParserFactory.getParser(HTMLParserFactory.XHTMLDOMParser);
        
        Date d1 = new Date();
        logger.info("getting page at " + d1.getTime());

        String[][] urlList = new String[][] {
            { "https", "www.microchipdirect.com", "/Chart.aspx?branchId=30044&mid=14&treeid=3101" },
            { "https", "www.schukat.com", "/schukat/schukat_cms_en.nsf/index/CMSDF15D356B046D53BC1256D550038A9E0?OpenDocument&wg=U1232&refDoc=CMS322921A477B31844C125707B0034EB15" },
        };

        Integer index = 0;

        URI uri = new URI(urlList[index][0], urlList[index][1], urlList[index][2], null);

        String url_str = uri.toASCIIString();
        url_str = url_str.replaceFirst("%3[f|F]", "?");//some servers couldn't understand encoded ? as query separator.
        logger.info("Encoded Url: " + url_str);
        //TODO:
//        InputStream pageStream = PageLoader.loadPage(url);
//        InputStream pageStream = PageLoader.loadPage("http://www.chinadaily.com.cn/index.html");
        
//        InputStream pageStream = PageLoader.loadPage("file://testPages/canon.html");//FAILED 
//        InputStream pageStream = PageLoader.loadPage("file://testPages/kodak.html");//WILL PASS
//        InputStream pageStream = PageLoader.loadPage("file://testPages/kodak1.html");//FAILED
//        InputStream pageStream = PageLoader.loadPage("file://testPages/nikon.html");//FAILED
//        InputStream pageStream = PageLoader.loadPage("file://testPages/polaroid.html");//failed
//        InputStream pageStream = PageLoader.loadPage("file://testPages/sanyo1.html");//failed
//        InputStream pageStream = PageLoader.loadPage("file://testPages/sony.html");//failed - not strict xhtml

        logger.info("got page in " + (new Date().getTime() - d1.getTime())/1000.0 + " seconds");
        d1 = new Date();

        logger.info("parsing  page at " + d1.getTime());

        org.jsoup.nodes.Document pageDomTreeJsoup = Jsoup.connect(url_str)
                .data("query", "Java")
                .userAgent("Mozilla")
                .cookie("auth", "token")
                .timeout(30000)
                .post();

        W3CDom w3cDom = new W3CDom();
        org.w3c.dom.Document pageDomTree = w3cDom.fromJsoup(pageDomTreeJsoup);

//        logger.info(pageDomTree);

        logger.info("parsed page in " + (new Date().getTime() - d1.getTime())/1000.0 + " seconds");

        TreeUtil tutil = new TreeUtil();
        Tree<String> pageTree = tutil.getTreeFromDOM(pageDomTree);

//        logger.info(pageTree);

        //Call MDR and FINDDR to identify all data regions
        int k = 10;//maximum combination
        float t = 0.3f;//edit distance threshold
        tutil.mdr(pageTree.getRoot(), k);
        tutil.findDR(pageTree.getRoot(), k, t);
        List<List<GeneralizedNode<Node<String>>>> drList = tutil.getDRs(pageTree);
        logger.info(drList.size());
        logger.info(drList);

        for (List<GeneralizedNode<Node<String>>> dr: drList) {
            /*for each data region */
            if (dr.get(0).size() == 1) {
                tutil.findRecord1(dr.get(0));// Case 1
            } else {
                tutil.findRecordN(dr.get(0));//Case 2
            }
        }

        Gson gson = new Gson();        

        //Data Extraction from DEPTA Section 4, Page# 80.
        /*
         * Produce one rooted tag tree for each data records for each data region.
         */
        for (List<GeneralizedNode<Node<String>>> dr: drList) {
            /*for each data region */
            PriorityQueue<Tree<String>> dataRecordQueue = tutil.buildDataRecrodTree(dr);
            tutil.partialTreeAlignment(dataRecordQueue);
            String dataRecordQueueJSON = gson.toJson((dataRecordQueue));
            
        }

//        logger.info(pageTree);

//        Tree<String> temp = pageTree.getSubTreeByPreOrder(55);
//        System.out.println(temp);

        logger.info("Done.");

            /*
            Source source = new DOMSource(pageDomTree);
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            logger.info(stringWriter.getBuffer().toString());
             */

        // pd.setUrl("http://www.amazon.com/s/ref=amb_link_85318851_27?ie=UTF8&node=565108&field-price=00000-39999&field-availability=-1&emi=ATVPDKIKX0DER&pf_rd_m=ATVPDKIKX0DER&pf_rd_s=center-6&pf_rd_r=143DDSYDRCRZ6D7DMYDQ&pf_rd_t=101&pf_rd_p=1288331602&pf_rd_i=565108");
        // pd.setUrl("http://www.w3.org/TR/xhtml1/");
    }
}