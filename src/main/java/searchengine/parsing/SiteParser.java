package searchengine.parsing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.lemmatizer.LemmaFinder;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.RecursiveTask;

import static java.lang.Thread.sleep;

public class SiteParser extends RecursiveTask<Integer> {

    private String page;
    private String siteName;
    private Site siteEntity;
    private int pageCount; //ToDo: переменная не реализована

    public static CopyOnWriteArraySet <String> allLinks = new CopyOnWriteArraySet<>(); //Todo: Зачем тут защита потока?
    private static PageRepository pageRepository;
    private static LemmaRepository lemmaRepository;

    private List<SiteParser> children;

    public SiteParser(String page, String siteName, Site siteEntity) {
        children = new ArrayList<>();
        this.page = page;
        this.siteName = siteName;
        this.siteEntity = siteEntity;
        allLinks.add(page);
    }

    public SiteParser(String siteName, Site siteEntity, PageRepository pageRepository, LemmaRepository lemmaRepository) {
        this(siteName, siteEntity.getUrl(), siteEntity);
        allLinks.add(siteEntity.getUrl());
        allLinks.add(siteEntity.getUrl() + "/");
        SiteParser.pageRepository = pageRepository;
        SiteParser.lemmaRepository = lemmaRepository;
    }

    @Override
    protected Integer compute() {
        try {
            sleep(random());
            Connection.Response response = Jsoup.connect(page)
                    .ignoreHttpErrors(true)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
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
        } catch (InterruptedException | IOException | NullPointerException ex) {
            ex.printStackTrace();
        }

        children.forEach(it -> {
                pageCount += it.join();
        });

        return pageCount;
    }

    public void addAdditionalPage() {
        try {
            Connection.Response response = Jsoup.connect(page)
                    .ignoreHttpErrors(true)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com") //ToDO: Создать конфиг
                    .execute();
            Document doc = response.parse();
            addPage(response, doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addPage(Connection.Response response, Document doc) {

        Page pageEntity = pageRepository.findByPath(page);
        if (pageEntity == null) {
            pageEntity = new Page();
        }

        pageEntity.setContent(doc.html());
        pageEntity.setPath(page);
        pageEntity.setCode(response.statusCode());
        pageEntity.setSite(siteEntity);
        pageRepository.save(pageEntity);

        if (response.statusCode() < 400) {
            LemmaFinder lemmaFinder = new LemmaFinder(pageEntity, siteEntity, lemmaRepository);
            lemmaFinder.collectLemmas();
        }
    }

    private void addChild(String url) {
        SiteParser child = new SiteParser(url, siteName, siteEntity);
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

    public void clearListOfLinks() {
        allLinks.clear();
    }
}

