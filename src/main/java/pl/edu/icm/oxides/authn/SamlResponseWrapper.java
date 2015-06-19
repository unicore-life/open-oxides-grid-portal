package pl.edu.icm.oxides.authn;

import eu.unicore.samly2.SAMLUtils;
import eu.unicore.samly2.exceptions.SAMLValidationException;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import xmlbeans.org.oasis.saml2.assertion.AssertionDocument;
import xmlbeans.org.oasis.saml2.assertion.AssertionType;
import xmlbeans.org.oasis.saml2.assertion.AttributeStatementType;
import xmlbeans.org.oasis.saml2.assertion.AttributeType;
import xmlbeans.org.oasis.saml2.assertion.NameIDType;
import xmlbeans.org.oasis.saml2.protocol.ResponseDocument;
import xmlbeans.org.oasis.saml2.protocol.ResponseType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SamlResponseWrapper {
    private List<AssertionDocument> authnAssertions = new ArrayList<>();
    private List<AssertionDocument> attributesAssertions = new ArrayList<>();
    private List<AssertionDocument> etdAssertions = new ArrayList<>();

    private SamlResponseAttributes attributes = new SamlResponseAttributes();

    public SamlResponseWrapper(ResponseDocument responseDocument) throws IOException, XmlException {
        AssertionDocument[] assertionDocuments = SAMLUtils.getAssertions(responseDocument.getResponse());
        for (AssertionDocument document : assertionDocuments) {
            AssertionType assertion = document.getAssertion();

            if (assertion.sizeOfAuthnStatementArray() > 0) {
                authnAssertions.add(document);
            }
            if (assertion.sizeOfAttributeStatementArray() > 0) {
                SamlResponseAttributes newAttributes = new SamlResponseAttributes();
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
                attributes.merge(newAttributes);
            }
        }
    }

    public List<AssertionDocument> getAuthnAssertions() {
        return authnAssertions;
    }

    public List<AssertionDocument> getAttributesAssertions() {
        return attributesAssertions;
    }

    public List<AssertionDocument> getEtdAssertions() {
        return etdAssertions;
    }

    public SamlResponseAttributes getAttributes() {
        return attributes;
    }


    // FIXME: do it a bit more like this
    private List<AssertionDocument> processAssertions(ResponseDocument responseDocument) throws IOException, XmlException, SAMLValidationException {
        List<AssertionDocument> assertionDocumentList = new ArrayList();

        ResponseType response = responseDocument.getResponse();
        NameIDType issuer = response.getIssuer();
        if (issuer != null && issuer.getFormat() != null && !issuer.getFormat().equals("urn:oasis:names:tc:SAML:2.0:nameid-format:entity")) {
            throw new SAMLValidationException("Issuer of SAML response must be of Entity type in SSO AuthN. It is: " + issuer.getFormat());
        } else {
//            SSOAuthnAssertionValidator authnAsValidator = new SSOAuthnAssertionValidator(this.consumerSamlName, this.consumerEndpointUri, this.requestId, this.samlValidityGraceTime, this.trustChecker, this.replayChecker, this.binding);
//            AssertionValidator asValidator = new AssertionValidator(this.consumerSamlName, this.consumerEndpointUri, (String)null, this.samlValidityGraceTime, this.trustChecker);

            AssertionDocument[] assertions = SAMLUtils.getAssertions(response);
            for (AssertionDocument assertionDocument : assertions) {
//                System.out.println(assertionDocument.xmlText());
                AssertionType assertion = assertionDocument.getAssertion();

                if (assertion.sizeOfAuthnStatementArray() > 0) {
//                    this.tryValidateAsAuthnAssertion(authnAsValidator, assertionDoc);
                    assertionDocumentList.add(assertionDocument);
                }

                if (assertion.sizeOfStatementArray() > 0 || assertion.sizeOfAttributeStatementArray() > 0 || assertion.sizeOfAuthzDecisionStatementArray() > 0) {
//                    this.tryValidateAsGenericAssertion(asValidator, assertionDoc);
                    assertionDocumentList.add(assertionDocument);
                }

                if (issuer == null) {
                    issuer = assertion.getIssuer();
                } else if (!issuer.getStringValue().equals(assertion.getIssuer().getStringValue())) {
                    throw new SAMLValidationException("Inconsistent issuer in assertion: " + assertion.getIssuer() + ", previously had: " + issuer);
                }
            }

//            if(this.authNAssertions.size() == 0) {
//                if(this.reasons.getSize() > 0) {
//                    throw new SAMLValidationException("Authentication assertion(s) was found, but it was not correct wrt SSO profile: " + this.reasons);
//                } else {
//                    throw new SAMLValidationException("There was no authentication assertion found in the SAML response");
//                }
//            }
        }
        return assertionDocumentList;
    }

//    protected void tryValidateAsGenericAssertion(AssertionValidator asValidator, AssertionDocument assertionDoc) throws SAMLValidationException {
//        asValidator.validate(assertionDoc);
//        AssertionType assertion = assertionDoc.getAssertion();
//        NameIDType asIssuer = assertion.getIssuer();
//        if(asIssuer.getFormat() != null && !asIssuer.getFormat().equals("urn:oasis:names:tc:SAML:2.0:nameid-format:entity")) {
//            throw new SAMLValidationException("Issuer of assertion must be of Entity type in SSO AuthN. It is: " + asIssuer.getFormat());
//        } else if(this.binding == SAMLBindings.HTTP_POST && (assertion.getSignature() == null || assertion.getSignature().isNil())) {
//            throw new SAMLValidationException("Assertion is not signed in the SSO authN used over HTTP POST, while should be.");
//        } else {
//            this.otherAssertions.add(assertionDoc);
//            if(assertion.sizeOfAttributeStatementArray() > 0) {
//                this.attributeAssertions.add(assertionDoc);
//            }
//
//        }
//    }

    private static final String TRUST_DELEGATION_ATTRIBUTE_NAME = "TrustDelegationOfUser";
}
