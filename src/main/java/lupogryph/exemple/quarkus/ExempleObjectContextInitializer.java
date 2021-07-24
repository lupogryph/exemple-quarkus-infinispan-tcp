package lupogryph.exemple.quarkus;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(
        includeClasses = {ExempleObject.class},
        schemaPackageName = "lupogryph.exemple.quarkus"
)
public interface ExempleObjectContextInitializer extends SerializationContextInitializer {
}
