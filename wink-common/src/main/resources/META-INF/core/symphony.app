# The priority of the providers is the following:
#   1. Media Type: n/m > n/* > *.*
#   2. Priority property, if the provider was loaded by SymphonyApplication,
#      by default the priority is set to 0.5
#   3. Order of the providers. The later provider was added, the higher priority it has.
#      This means that if this file is added by a single Application, which occurs by default,
#      the later provider appears in this file, the higher priority it has.

# JAX-RS Required Providers
org.apache.wink.common.internal.providers.entity.FileProvider
org.apache.wink.common.internal.providers.entity.ByteArrayProvider
org.apache.wink.common.internal.providers.entity.InputStreamProvider
org.apache.wink.common.internal.providers.entity.StringProvider
org.apache.wink.common.internal.providers.entity.ReaderProvider
org.apache.wink.common.internal.providers.entity.DataSourceProvider
org.apache.wink.common.internal.providers.entity.FormMultivaluedMapProvider
org.apache.wink.common.internal.providers.entity.SourceProvider$StreamSourceProvider
org.apache.wink.common.internal.providers.entity.SourceProvider$SAXSourceProvider
org.apache.wink.common.internal.providers.entity.SourceProvider$DOMSourceProvider
org.apache.wink.common.internal.providers.entity.StreamingOutputProvider


# JAXB Providers
org.apache.wink.common.internal.providers.entity.xml.JAXBElementXmlProvider
org.apache.wink.common.internal.providers.entity.xml.JAXBXmlProvider

# Atom 
org.apache.wink.common.internal.providers.entity.atom.AtomFeedProvider
org.apache.wink.common.internal.providers.entity.atom.AtomFeedSyndFeedProvider
org.apache.wink.common.internal.providers.entity.atom.AtomFeedJAXBElementProvider
org.apache.wink.common.internal.providers.entity.atom.AtomEntryProvider
org.apache.wink.common.internal.providers.entity.atom.AtomEntrySyndEntryProvider
org.apache.wink.common.internal.providers.entity.atom.AtomEntryJAXBElementProvider

# APP
org.apache.wink.common.internal.providers.entity.app.AppServiceProvider
org.apache.wink.common.internal.providers.entity.app.AppCategoriesProvider
org.apache.wink.common.internal.providers.entity.app.CategoriesProvider

# Open Search
org.apache.wink.common.internal.providers.entity.opensearch.OpenSearchDescriptionProvider

# Json
org.apache.wink.common.internal.providers.entity.json.JsonProvider
org.apache.wink.common.internal.providers.entity.json.JsonJAXBProvider
org.apache.wink.common.internal.providers.entity.json.JsonSyndEntryProvider
org.apache.wink.common.internal.providers.entity.json.JsonSyndFeedProvider

# Asset Provider
org.apache.wink.common.internal.providers.entity.AssetProvider

# Html
org.apache.wink.server.internal.providers.entity.html.HtmlProvider
#org.apache.wink.server.internal.providers.entity.html.HtmlSyndEntryProvider
#org.apache.wink.server.internal.providers.entity.html.HtmlSyndFeedProvider

# CSV
org.apache.wink.common.internal.providers.entity.csv.CsvSerializerProvider
org.apache.wink.common.internal.providers.entity.csv.CsvSyndFeedSerializerProvider
org.apache.wink.common.internal.providers.entity.csv.CsvDeserializerProvider

# Exception
org.apache.wink.common.internal.providers.entity.FormatedExceptionProvider



