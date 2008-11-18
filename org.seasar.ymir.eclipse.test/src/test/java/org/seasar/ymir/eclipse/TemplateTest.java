package org.seasar.ymir.eclipse;

import java.beans.Introspector;
import java.io.InputStreamReader;
import java.io.StringWriter;

import junit.framework.TestCase;
import net.skirnir.xom.BeanAccessor;
import net.skirnir.xom.BeanAccessorFactory;
import net.skirnir.xom.XMLParserFactory;
import net.skirnir.xom.XOMapper;
import net.skirnir.xom.XOMapperFactory;
import net.skirnir.xom.annotation.impl.AnnotationBeanAccessor;

public class TemplateTest extends TestCase {
    private XOMapper mapper = XOMapperFactory.newInstance().setBeanAccessorFactory(new BeanAccessorFactory() {
        public BeanAccessor newInstance() {
            return new AnnotationBeanAccessor() {
                @Override
                protected String toXMLName(String javaName) {
                    return Introspector.decapitalize(javaName);
                }
            };
        }
    }).setStrict(true);

    private TemplateEntry createTemplate() {
        TemplateEntry template = new TemplateEntry();
        template.setSkeletons(new SkeletonEntries(new SkeletonEntry("ymir-skeleton-generic", "標準のYmirプロジェクト",
                "標準のYmirプロジェクトを作成します。テンプレートエンジンにはFreyja ZPTエンジンを使用します。"), new SkeletonEntry("ymir-skeleton-generic",
                "Ymir+DBFluteプロジェクト",
                "標準のYmirプロジェクトを作成します。テンプレートエンジンにはFreyja ZPTエンジンを使用します。データベースアクセスにはDBFluteを使用します。", new FragmentEntry(
                        "ymir-fragment-dbflute", "", "")), new SkeletonEntry("ymir-skeleton-skeleton",
                "Viliスケルトンプロジェクト", "Viliのプロジェクトスケルトンを作成するためのプロジェクトを作成します。"), new SkeletonEntry(
                "ymir-skeleton-fragment", "Viliフラグメントプロジェクト", "Viliのプロジェクトフラグメントを作成するためのプロジェクトを作成します。")));
        template.setFragments(new FragmentEntries(new FragmentEntry("ymir-fragment-utility", "ユーティリティ",
                "アプリケーションを効率良く開発するためのユーティリティクラスを追加します。"), new FragmentEntry("ymir-fragment-dbflute", "DBFlute",
                "DBFluteを使ってデータベースにアクセスする機能を追加します。"), new FragmentEntry("ymir-fragment-json", "JSON連携",
                "JSONリクエストを受け取ったりJSONレスポンスを返したりする機能を追加します。"), new FragmentEntry("ymir-fragment-amf", "AMF連携",
                "AMFプロトコルで通信する機能を追加します。FlexからPageオブジェクトのメソッドを呼び出すことができるようになります。")));

        return template;
    }

    public void testToBean() throws Exception {
        TemplateEntry actual = mapper.toBean(XMLParserFactory.newInstance().parse(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream(
                        getClass().getName().replace('.', '/').concat("_template1.xml")), "UTF-8")).getRootElement(),
                TemplateEntry.class);

        StringWriter sw = new StringWriter();
        mapper.toXML(actual, sw);
        String actualString = sw.toString();

        sw = new StringWriter();
        mapper.toXML(createTemplate(), sw);
        String expectedString = sw.toString();

        assertEquals(expectedString, actualString);
    }
}
