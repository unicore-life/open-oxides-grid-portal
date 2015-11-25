package pl.edu.icm.oxides.authn;

import eu.unicore.samly2.SAMLUtils;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import xmlbeans.org.oasis.saml2.assertion.AssertionDocument;
import xmlbeans.org.oasis.saml2.assertion.AssertionType;
import xmlbeans.org.oasis.saml2.assertion.AttributeStatementType;
import xmlbeans.org.oasis.saml2.assertion.AttributeType;
import xmlbeans.org.oasis.saml2.protocol.ResponseDocument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class EtdAssertionsWrapper {
    private final List<AssertionDocument> authnAssertions = new ArrayList<>();
    private final List<AssertionDocument> attributesAssertions = new ArrayList<>();
    private final List<AssertionDocument> etdAssertions = new ArrayList<>();

    private final EtdAttributeData attributeData = EtdAttributeData.Builder.build();

    EtdAssertionsWrapper(ResponseDocument responseDocument) throws IOException, XmlException {
        AssertionDocument[] assertionDocuments = SAMLUtils.getAssertions(responseDocument.getResponse());
        for (AssertionDocument document : assertionDocuments) {
            AssertionType assertion = document.getAssertion();

            if (assertion.sizeOfAuthnStatementArray() > 0) {
                authnAssertions.add(document);
            }
            if (assertion.sizeOfAttributeStatementArray() > 0) {
                EtdAttributeData newAttributes = EtdAttributeData.Builder.build();
                for (AttributeStatementType statement : assertion.getAttributeStatementArray()) {
                    for (AttributeType attribute : statement.getAttributeArray()) {
                        String attributeName = attribute.getName();
                        for (XmlObject object : attribute.getAttributeValueArray()) {
                            String attributeValue = ((SimpleValue) object).getStringValue();
                            newAttributes.put(attributeName, attributeValue);
                        }
                    }
                }

                if (newAttributes.containsKey(TRUST_DELEGATION_ATTRIBUTE_NAME)) {
                    etdAssertions.add(document);
                } else {
                    attributesAssertions.add(document);
                }
                attributeData.merge(newAttributes);
            }
        }
    }

    List<AssertionDocument> getAuthnAssertions() {
        return authnAssertions;
    }

    List<AssertionDocument> getAttributesAssertions() {
        return attributesAssertions;
    }

    List<AssertionDocument> getEtdAssertions() {
        return etdAssertions;
    }

    EtdAttributeData getAttributeData() {
        return attributeData;
    }

    private static final String TRUST_DELEGATION_ATTRIBUTE_NAME = "TrustDelegationOfUser";
}
