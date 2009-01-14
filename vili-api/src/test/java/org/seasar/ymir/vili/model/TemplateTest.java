package org.seasar.ymir.vili.model;

import java.io.InputStreamReader;
import java.io.StringWriter;

import junit.framework.TestCase;

import org.seasar.ymir.vili.util.XOMUtils;

import net.skirnir.xom.XMLParserFactory;

public class TemplateTest extends TestCase {
    private Template createTemplate() {
        Template template = new Template();
        template
                .setSkeletons(new Skeletons(
                        new Skeleton("ymir-skeleton-generic", "標準のYmirプロジェクト",
                                "標準のYmirプロジェクトを作成します。テンプレートエンジンにはFreyja ZPTエンジンを使用します。"),
                        new Skeleton(
                                "ymir-skeleton-generic",
                                "Ymir+DBFluteプロジェクト",
                                "標準のYmirプロジェクトを作成します。テンプレートエンジンにはFreyja ZPTエンジンを使用します。データベースアクセスにはDBFluteを使用します。",
                                new Fragment("ymir-fragment-dbflute", "", "")),
                        new Skeleton("ymir-skeleton-skeleton",
                                "Viliスケルトンプロジェクト",
                                "Viliのプロジェクトスケルトンを作成するためのプロジェクトを作成します。"),
                        new Skeleton("ymir-skeleton-fragment",
                                "Viliフラグメントプロジェクト",
                                "Viliのプロジェクトフラグメントを作成するためのプロジェクトを作成します。")));
        template
                .setFragments(new Fragments(
                        new Fragment("ymir-fragment-utility", "ユーティリティ",
                                "アプリケーションを効率良く開発するためのユーティリティクラスを追加します。"),
                        new Fragment("ymir-fragment-dbflute", "DBFlute",
                                "DBFluteを使ってデータベースにアクセスする機能を追加します。"),
                        new Fragment("ymir-fragment-json", "JSON連携",
                                "JSONリクエストを受け取ったりJSONレスポンスを返したりする機能を追加します。"),
                        new Fragment("ymir-fragment-amf", "AMF連携",
                                "AMFプロトコルで通信する機能を追加します。FlexからPageオブジェクトのメソッドを呼び出すことができるようになります。")));

        return template;
    }

    public void testToBean() throws Exception {
        Template actual = XOMUtils.getXOMapper().toBean(
                XMLParserFactory.newInstance().parse(
                        new InputStreamReader(getClass().getClassLoader()
                                .getResourceAsStream(
                                        getClass().getName().replace('.', '/')
                                                .concat("_template1.xml")),
                                "UTF-8")).getRootElement(), Template.class);

        StringWriter sw = new StringWriter();
        XOMUtils.getXOMapper().toXML(actual, sw);
        String actualString = sw.toString();

        sw = new StringWriter();
        XOMUtils.getXOMapper().toXML(createTemplate(), sw);
        String expectedString = sw.toString();

        assertEquals(expectedString, actualString);
    }
}
