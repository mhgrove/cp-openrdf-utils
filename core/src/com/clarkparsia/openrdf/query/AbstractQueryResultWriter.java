// Copyright (c) 2010 - 2013, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// For more information about licensing and copyright of this software, please contact
// inquiries@clarkparsia.com or visit http://stardog.com

package com.clarkparsia.openrdf.query;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultFormat;
import org.openrdf.query.resultio.QueryResultWriter;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;

/**
 * <p>More useful base for creating custom {@link QueryResultWriter} implementations.
 * Provides no-op methods for the new parts of the API so existing implementations don't
 * have boilerplate no-op methods to ignore the stuff that should not be in the API.</p>
 *
 * @author  Michael Grove
 * @since   1.0
 * @version 1.0
 */
public abstract class AbstractQueryResultWriter implements QueryResultWriter {

    private final QueryResultFormat mFormat;

    private WriterConfig mWriterConfig = new WriterConfig();

    protected AbstractQueryResultWriter(final QueryResultFormat theFormat) {
        mFormat = theFormat;
    }

    /**
     * @inheritDoc
     */
    @Override
    public final QueryResultFormat getQueryResultFormat() {
        return mFormat;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void handleStylesheet(final String s) throws QueryResultHandlerException {
        // no-op
    }

    /**
     * @inheritDoc
     */
    @Override
    public void setWriterConfig(WriterConfig config) {
        this.mWriterConfig = config;
    }

    /**
     * @inheritDoc
     */
    @Override
    public WriterConfig getWriterConfig() {
        return this.mWriterConfig;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Collection<RioSetting<?>> getSupportedSettings() {
        return Collections.emptyList();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void handleLinks(final List<String> theStrings) throws QueryResultHandlerException {
        // no-op
    }

    /**
     * @inheritDoc
     */
    @Override
    public void startHeader() throws QueryResultHandlerException {
        // no-op
    }

    /**
     * @inheritDoc
     */
    @Override
    public void startDocument() throws QueryResultHandlerException {
        // no-op
    }

    /**
     * @inheritDoc
     */
    @Override
    public void handleNamespace(final String s, final String s2) throws QueryResultHandlerException {
        // no-op
    }

    /**
     * @inheritDoc
     */
    @Override
    public void endHeader() throws QueryResultHandlerException {
        // no-op
    }

    /**
     * @inheritDoc
     */
    @Override
    public void handleBoolean(final boolean b) throws QueryResultHandlerException {
        throw new UnsupportedOperationException();
    }
}
