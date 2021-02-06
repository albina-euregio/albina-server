package eu.albina.util;

import eu.albina.model.PushSubscription;
import eu.albina.model.enumerations.LanguageCode;
import org.junit.Ignore;
import org.junit.Test;

public class PushNotificationUtilTest {

	@Test
	@Ignore
	public void test() {
		PushSubscription subscription = new PushSubscription();
		GlobalVariables.loadConfigProperties();
		subscription.setAuth("wnAO8hfJGyGtdK3uUmVI8g");
		subscription.setEndpoint("https://updates.push.services.mozilla.com/wpush/v2/gAAAAABgHwSx9txJscXfY5Dz82G5Xs7b6U0zROFXDPDhSM9D4KCTEmGxJTLfZ7arYnRlS3BexTWFeLA8pfzDEHjd8tX9UBmLuUaR3Xnim3Q-2Xa3UddaHRbh4NT2mKFMGBDmIZ4208OgpVECiuoI8UANC9B3IOf2CpduP58fUz1VE857gyNeHsw");
		subscription.setLanguage(LanguageCode.de);
		subscription.setP256dh("BEoQn2VR93GQ9gBxOo4pvdmgOyO1eiSDjUy7blwez1Vu_99PDswkEtV6m7cuwB60A8WlYq6lGKTZLet7PbnAEow");
		subscription.setRegion("AT-07");
		PushNotificationUtil.getInstance().sendWelcomePushMessage(subscription);
	}
}
