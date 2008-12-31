package org.seasar.ymir.eclipse.util;

import java.beans.Introspector;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;

import net.skirnir.xom.BeanAccessor;
import net.skirnir.xom.BeanAccessorFactory;
import net.skirnir.xom.XMLParser;
import net.skirnir.xom.XMLParserFactory;
import net.skirnir.xom.XOMapper;
import net.skirnir.xom.XOMapperFactory;
import net.skirnir.xom.annotation.impl.AnnotationBeanAccessor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.seasar.ymir.vili.Globals;

public class XOMUtils {
    private static final XOMapper mapper = XOMapperFactory.newInstance().setBeanAccessorFactory(
            new BeanAccessorFactory() {
                public BeanAccessor newInstance() {
                    return new AnnotationBeanAccessor() {
                        @Override
                        protected String toXMLName(String javaName) {
                            return Introspector.decapitalize(javaName);
                        }
                    };
                }
            }).setStrict(false).setTrimContent(true);

    private static final XMLParser parser = XMLParserFactory.newInstance();

    private XOMUtils() {
    }

    public static XOMapper getXOMapper() {
        return mapper;
    }

    public static XMLParser getXMLParser() {
        return parser;
    }

    public static <T> T getAsBean(String content, Class<T> clazz) throws CoreException {
        if (content == null || content.trim().length() == 0) {
            return null;
        }
        return getAsBean(new StringReader(content), clazz);
    }

    public static <T> T getAsBean(Reader reader, Class<T> clazz) throws CoreException {
        try {
            return mapper.toBean(parser.parse(reader).getRootElement(), //$NON-NLS-1$
                    clazz);
        } catch (Throwable t) {
            throw new CoreException(new Status(IStatus.ERROR, Globals.PLUGIN_ID, t.toString(), t));
        }
    }

    public static <T> T getAsBean(IFile file, Class<T> clazz) throws CoreException {
        if (!file.exists()) {
            return null;
        }

        try {
            return getAsBean(new InputStreamReader(file.getContents(), "UTF-8"), clazz);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Can't happen!", ex);
        }
    }
}
