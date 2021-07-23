package lupogryph.exemple.quarkus;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
        includeClasses = {ExempleObject.class},
        schemaPackageName = "fr.icefeather.exemple.quarkus.cache"
)
public interface ExempleObjectContextInitializer extends SerializationContextInitializer {
}
