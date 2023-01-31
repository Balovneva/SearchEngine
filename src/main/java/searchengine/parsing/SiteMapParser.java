package searchengine.parsing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.Site;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;

import static java.lang.Thread.sleep;

//Todo: нужна аннотация @Component?
public class SiteMapParser extends RecursiveTask<Integer> {

    private String page;
    private String siteName;
    private SiteEntity siteEntity;
    private int pageCount; //ToDo: переменная не реализована

    public static CopyOnWriteArraySet <String> allLinks = new CopyOnWriteArraySet<>();
    private static PageRepository pageRepository;

    private List<SiteMapParser> children;

    public SiteMapParser(String page, String siteName, SiteEntity siteEntity) {
        children = new ArrayList<>();
        this.page = page;
        this.siteName = siteName;
        this.siteEntity = siteEntity;
        allLinks.add(page);
    }

    public SiteMapParser(String siteName, SiteEntity siteEntity, PageRepository pageRepository) {
        this(siteName, siteName, siteEntity);
        allLinks.add(siteName + "/");
        SiteMapParser.pageRepository = pageRepository;
    }

    @Override
    protected Integer compute() {
        try {
            sleep(random());
            Connection.Response response = Jsoup.connect(page)
                    .ignoreHttpErrors(true)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com") //ToDo: характеристики убррать в application.yaml
                    .execute();
            Document doc = response.parse();

            addPage(response, doc);

            Elements links = doc.select("a");

            for (Element link : links) {
                String url = link.attr("href");

                if (!url.contains("http")) {
                    if (!url.startsWith("/") && url.length() > 1) {
                        url = "/" + url;
                    }

                    url = siteName + url;
                }

                if (isCorrected(url)) {
                    addChild(url);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        children.forEach(it -> {
                pageCount += it.join();
        });

        return pageCount;
    }

    private void addPage(Connection.Response response, Document doc) {
        PageEntity pageEntity = new PageEntity();
        pageEntity.setContent(doc.html());
        pageEntity.setPath(page);
        pageEntity.setCode(response.statusCode());
        pageEntity.setSiteEntity(siteEntity);
        pageRepository.save(pageEntity);
    }

    private void addChild(String url) {
        SiteMapParser child = new SiteMapParser(url, siteName, siteEntity);
        children.add(child);
        child.fork();
    }

    private boolean isCorrected(String url) {
        return (url.contains(siteName) &&
                !allLinks.contains(url) &&
                !url.contains("#") &&
                !url.matches("n+(.jpg|.jpeg|.png|.pdf|gif|.zip|.tar|.jar|.gz|.svg|ppt|.pptx)"));
    }

    private int random() {
        return (int) Math.round(Math.random() * 51 + 100);
    }
}

