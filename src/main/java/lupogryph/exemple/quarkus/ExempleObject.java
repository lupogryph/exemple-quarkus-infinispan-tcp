package lupogryph.exemple.quarkus;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class ExempleObject implements Serializable {

    @ProtoField(number = 1)
    String nom;

    @ProtoField(number = 2)
    String prenom;

    @ProtoField(number = 3)
    String adresse;

    @ProtoFactory
    public ExempleObject(String nom, String prenom, String adresse) {
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
    }
}

