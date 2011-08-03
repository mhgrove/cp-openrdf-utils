/*
 * Copyright (c) 2005-2010 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.openrdf.query.results;

import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.impl.TupleQueryResultImpl;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;

import com.clarkparsia.utils.collections.CollectionUtil;

/**
 * <p>Sax implementation to parse a SPARQL XML result set into a result set.</p>
 *
 * @author Michael Grove
 * @since 0.1
 * @version 0.3
 */
@Deprecated
public class SparqlXmlResultSetParser extends DefaultHandler {
	private ResultSetBuilder mResults;

    private String mElementString;
    private String mBindingName;
    private String mLang;
    private String mDatatype;
	private Boolean mBooleanResult = null;

    private static final String RESULTS = "http://www.w3.org/2005/sparql-results#results";
    private static final String RESULT = "http://www.w3.org/2005/sparql-results#result";
    private static final String BINDING = "http://www.w3.org/2005/sparql-results#binding";
    private static final String TYPE_LITERAL = "http://www.w3.org/2005/sparql-results#literal";
    private static final String TYPE_BNODE = "http://www.w3.org/2005/sparql-results#bnode";
	private static final String BOOLEAN_RESULT = "http://www.w3.org/2005/sparql-results#boolean";
    private static final String TYPE_URI = "http://www.w3.org/2005/sparql-results#uri";
    private static final String NAME = "name";
    private static final String LANG = "xml:lang";
    private static final String DATATYPE = "datatype";

	public SparqlXmlResultSetParser() {
		this(new ValueFactoryImpl());
	}

	public SparqlXmlResultSetParser(ValueFactoryImpl theFactory) {
		mResults = new ResultSetBuilder(theFactory);
	}

	/**
	 * @inheritDoc
	 */
	@Override
    public void startDocument() {
		mBooleanResult = null;
        mResults.startResultSet();
    }

	/**
	 * @inheritDoc
	 */
	@Override
    public void endDocument() {
		mResults.endResultSet();
    }

	public List<String> bindingNames() {
		return mResults.bindingNames();
	}

	public Collection<BindingSet> bindingSet() {
		return mResults.bindingSet();
	}

	public TupleQueryResult tupleResult() {
		return new TupleQueryResultImpl(mResults.bindingNames(), mResults.bindingSet());
	}

	public boolean booleanResult() {
		return mBooleanResult == null ? false : mBooleanResult;
	}

	public boolean isBooleanResult() {
		return mBooleanResult != null;
	}

	/**
	 * @inheritDoc
	 */
	@Override
    public void startElement(String theURI, String theLocalName, String theQName, Attributes theAttrs) {
        String aURI = theURI + theLocalName;

        if (aURI.equals(BINDING)) {
            mBindingName = theAttrs.getValue(NAME).replaceAll("\\?","");
        }
        else if (aURI.equals(TYPE_LITERAL)) {
            mLang = theAttrs.getValue(LANG);
            mDatatype = theAttrs.getValue(DATATYPE);
            mElementString = "";
        }
        else if (aURI.equals(TYPE_URI)) {
            mElementString = "";
        }
        else if (aURI.equals(TYPE_BNODE)) {
            mElementString = "";
        }
        else if (aURI.equals(RESULT)) {
			mResults.startBinding();
        }
		else if (aURI.equals(BOOLEAN_RESULT)) {
			mElementString = "";
		}
    }

	/**
	 * @inheritDoc
	 */
	@Override
    public void endElement(String theURI, String theLocalName, String theQName) {
        String aURI = theURI + theLocalName;

        if (aURI.equals(RESULT)) {
			mResults.endBinding();
        }
		else if (aURI.equals(BOOLEAN_RESULT)) {
			mBooleanResult = Boolean.valueOf(mElementString.trim());
		}
        else if (aURI.equals(TYPE_URI)) {
			mResults.addToBinding(mBindingName, mResults.getValueFactory().createURI(mElementString));
        }
        else if (aURI.equals(TYPE_LITERAL)) {
            Literal aLiteral = null;

            if (mLang == null && mDatatype == null) {
                aLiteral = mResults.getValueFactory().createLiteral(mElementString);
            }
            else if (mLang != null) {
                aLiteral = mResults.getValueFactory().createLiteral(mElementString, mLang);
            }
            else if (mDatatype != null) {
                aLiteral = mResults.getValueFactory().createLiteral(mElementString, mResults.getValueFactory().createURI(mDatatype));
            }

            mResults.addToBinding(mBindingName, aLiteral);
        }
        else if (aURI.equals(TYPE_BNODE)) {
            mResults.addToBinding(mBindingName, mResults.getValueFactory().createBNode(mElementString));
        }
    }

	/**
	 * @inheritDoc
	 */
	@Override
    public void characters(char[] theChars, int theStart, int theLength) {
		if (mElementString == null) {
			mElementString = "";
		}

        StringBuffer aBuffer = new StringBuffer();

        for (int i = 0; i < theLength; i++)
            aBuffer.append(theChars[theStart + i]);

        mElementString += aBuffer.toString();
    }

	class ResultSetBuilder {
		private Collection<BindingSet> mValues = new ArrayList<BindingSet>();
		private MapBindingSet mCurrBinding = new MapBindingSet();
		private ValueFactory mFactory;

		public ResultSetBuilder(final ValueFactory theFactory) {
			mFactory = theFactory;
		}

		public void reset() {
			mValues.clear();
		}

		public void startResultSet() {
			reset();
		}

		public void endResultSet() {
		}

		public void startBinding() {
			mCurrBinding = new MapBindingSet();
		}

		public ValueFactory getValueFactory() {
			return mFactory;
		}

		public void endBinding() {
			mValues.add(mCurrBinding);
		}

		public ResultSetBuilder addToBinding(String theKey, Value theValue) {
			mCurrBinding.addBinding(theKey, theValue);

			return this;
		}

		public Collection<BindingSet> bindingSet() {
			return mValues;
		}

		public List<String> bindingNames() {
			if (mValues.isEmpty()) {
				return Collections.emptyList();
			}
			else {
				return CollectionUtil.list(mValues.iterator().next().getBindingNames());
			}
		}
	}
}
