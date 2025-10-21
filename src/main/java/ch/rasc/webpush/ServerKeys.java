package ch.rasc.webpush;

/**
 * VAPID keys from https://vapidkeys.com/
 *
 * @apiNote https://developers.google.com/web/fundamentals/push-notifications/web-push-protocol#application_server_keys
 */
public record ServerKeys(String publicKeyBase64, String privateKeyBase64) {
}
