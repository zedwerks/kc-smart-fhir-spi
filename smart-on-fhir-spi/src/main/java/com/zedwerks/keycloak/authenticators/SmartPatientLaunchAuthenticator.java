package com.zedwerks.keycloak.authenticators;

import java.util.stream.Stream;
import java.util.ArrayList;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.protocol.oidc.TokenManager;

public class SmartPatientLaunchAuthenticator implements Authenticator {

    public static final Logger logger = Logger.getLogger(SmartPatientLaunchAuthenticator.class);

    public SmartPatientLaunchAuthenticator(KeycloakSession session) {
        logger.info("SmartPatientLaunchAuthenticator(session) **** SMART on FHIR Patient-Launch Authenticator ****");
        // NOOP
    }

    public SmartPatientLaunchAuthenticator() {
        logger.info("SmartPatientLaunchAuthenticator() **** SMART on FHIR Patient-Launch Authenticator ****");
        // NOOP
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        logger.info("authenticate() **** SMART on FHIR Patient-Launch Authenticator ****");

        // First, let's clear out any launch context, patient_id, etc...
        SmartOnFhir.clearSmartLaunchInSession(context);

        if (!SmartOnFhir.isSmartOnFhirRequest(context)) {
            logger.info("*** SMART on FHIR Patient-Launch Authenticator: This is not a SMART on FHIR request.");
            context.attempted(); // just carry on... not a SMART on FHIR request
            return;
        }

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        ClientModel client = authSession.getClient();

        String requestedScopesString = authSession.getClientNote(OIDCLoginProtocol.SCOPE_PARAM);
        Stream<ClientScopeModel> clientScopes = TokenManager.getRequestedClientScopes(requestedScopesString, client);

        logger.info("Requested Scopes: " + requestedScopesString);

        if (clientScopes == null) {
            logger.info("No scopes found");
            context.attempted(); // just carry on... not a SMART on FHIR launch request
            return;
        }

        ArrayList<String> scopes = new ArrayList<String>();
        clientScopes.forEach(scope -> scopes.add(scope.getName()));

        if (!scopes.contains(SmartOnFhir.SMART_SCOPE_LAUNCH_PATIENT)) {
            logger.info("Not a SMART on FHIR launch/patient request");
            context.attempted(); // just carry on... not a SMART on FHIR standalone launch/patient request
            return;
        }

        // Okay, so let's do this! Time to ask the user to select a patient!!!

        logger.info("SMART on FHIR launch/patient request found!");
        context.attempted();
    }

    /* @todo for future processing...
    private void fail(AuthenticationFlowContext context, String msg) {
        logger.warn(msg);
        context.failure(AuthenticationFlowError.INTERNAL_ERROR,
                Response.status(302)
                        .header("Location", context.getAuthenticationSession().getRedirectUri() +
                                "?error=server_error" +
                                "&error_description=" + msg)
                        .build());
    }

    private void succeed(AuthenticationFlowContext context, String patient) {
        logger.info("SMART on FHIR launch/patient request succeeded!");
        // Add selected information to authentication session
        context.getAuthenticationSession().setUserSessionNote("patient_id", patient);
        context.success();
    } */

    @Override
    public boolean requiresUser() {
        logger.info("requiresUser() **** SMART on FHIR EHR-Launch Authenticator ****");
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.info("configuredFor() **** SMART on FHIR EHR-Launch Authenticator ****");
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        logger.info("setRequiredActions() **** SMART on FHIR EHR-Launch Authenticator ****");
        // NOOP
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        logger.info("action() **** SMART on FHIR EHR-Launch Authenticator ****");
        // NOOP
    }

    @Override
    public void close() {
        logger.info("close() **** SMART on FHIR EHR-Launch Authenticator ****");
        // NOOP
    }

}
