package eu.albina.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.albina.model.DangerSourceVariant;
import eu.albina.model.DangerSourceVariantText;
import eu.albina.model.EawsMatrixInformation;
import eu.albina.model.enumerations.Aspect;
import eu.albina.model.enumerations.AvalancheSize;
import eu.albina.model.enumerations.AvalancheType;
import eu.albina.model.enumerations.Frequency;
import eu.albina.model.enumerations.GlidingSnowActivity;

public class DangerSourceVariantTextControllerTest {

	private DangerSourceVariantTextController controller;
	private DangerSourceVariant dangerSourceVariant;
	private DangerSourceVariantText dangerSourceVariantText;
	private EawsMatrixInformation eawsMatrixInformation;

	@BeforeEach
	public void setUp() {
		controller = DangerSourceVariantTextController.getInstance();
		dangerSourceVariant = mock(DangerSourceVariant.class);
        eawsMatrixInformation = mock(EawsMatrixInformation.class);
		dangerSourceVariantText = mock(DangerSourceVariantText.class);
	}

	@Test
	public void testGetDangerSourceVariantTextForGlideAvalanche() {
        when(eawsMatrixInformation.getAvalancheSize()).thenReturn(AvalancheSize.medium);
        when(eawsMatrixInformation.getFrequency()).thenReturn(Frequency.many);
		when(dangerSourceVariant.getAvalancheType()).thenReturn(AvalancheType.glide);
		when(dangerSourceVariant.getGlidingSnowActivity()).thenReturn(GlidingSnowActivity.high);
        when(dangerSourceVariant.getAspects()).thenReturn(Set.of(Aspect.N, Aspect.NE, Aspect.NW));
        when(dangerSourceVariant.getElevationHigh()).thenReturn(2400);
		when(dangerSourceVariant.getEawsMatrixInformation()).thenReturn(eawsMatrixInformation);

		when(dangerSourceVariantText.getTextcat()).thenReturn("{\"curlyName\":\"Nassschnee02\",\"line\":0,\"args\":{\"es_warum_Ort_wann\":{\"curlyName\":\"es_warum_Ort_wann\",\"line\":0},\"oberhalb_von_Höhe_optional\":{\"curlyName\":\"oberhalb_von_Höhe_optional\",\"line\":0},\"wieviele1\":{\"curlyName\":\"wieviele1\",\"line\":1},\"Grösse_der_Lawinen\":{\"curlyName\":\"Grösse_der_Lawinen\",\"line\":5},\"Lawinenart5\":{\"curlyName\":\"Lawinenart5\",\"line\":45},\"Nassschnee02§möglich\":{\"curlyName\":\"Nassschnee02§möglich\",\"line\":1}}},{\"curlyName\":\"Dies05\",\"line\":0,\"args\":{\"Dies2\":{\"curlyName\":\"Dies2\",\"line\":4},\"aus_unter\":{\"curlyName\":\"aus_unter\",\"line\":0},\"steil\":{\"curlyName\":\"steil\",\"line\":2},\"hoch_gelegenen\":{\"curlyName\":\"hoch_gelegenen\",\"line\":0},\"nordseitig\":{\"curlyName\":\"nordseitig\",\"line\":1,\"args\":{\"Komma_und°2\":{\"curlyName\":\"Komma_und°2\",\"line\":0}}},\"Komma_und3\":{\"curlyName\":\"Komma_und3\",\"line\":0},\"entladen\":{\"curlyName\":\"entladen\",\"line\":0},\"Höhe_Höhenlage\":{\"curlyName\":\"Höhe_Höhenlage\",\"line\":1,\"args\":{\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":0}}}}},{\"curlyName\":\"Gleitschnee01\",\"line\":0,\"args\":{\"Gleitschnee01§Zonen_unterhalb\":{\"curlyName\":\"Gleitschnee01§Zonen_unterhalb\",\"line\":4}}},{\"curlyName\":\"Gleitschnee01\",\"line\":0,\"args\":{\"Gleitschnee01§Zonen_unterhalb\":{\"curlyName\":\"Gleitschnee01§Zonen_unterhalb\",\"line\":2}}}");

		String result = controller.getTextForDangerSourceVariant(dangerSourceVariant, dangerSourceVariantText);
		assertEquals("{\"curlyName\":\"Nassschnee02\",\"line\":0,\"args\":{\"es_warum_Ort_wann\":{\"curlyName\":\"es_warum_Ort_wann\",\"line\":0},\"oberhalb_von_Höhe_optional\":{\"curlyName\":\"oberhalb_von_Höhe_optional\",\"line\":0},\"wieviele1\":{\"curlyName\":\"wieviele1\",\"line\":1},\"Grösse_der_Lawinen\":{\"curlyName\":\"Grösse_der_Lawinen\",\"line\":5},\"Lawinenart5\":{\"curlyName\":\"Lawinenart5\",\"line\":45},\"Nassschnee02§möglich\":{\"curlyName\":\"Nassschnee02§möglich\",\"line\":1}}},{\"curlyName\":\"Dies05\",\"line\":0,\"args\":{\"Dies2\":{\"curlyName\":\"Dies2\",\"line\":4},\"aus_unter\":{\"curlyName\":\"aus_unter\",\"line\":0},\"steil\":{\"curlyName\":\"steil\",\"line\":2},\"hoch_gelegenen\":{\"curlyName\":\"hoch_gelegenen\",\"line\":0},\"nordseitig\":{\"curlyName\":\"nordseitig\",\"line\":1,\"args\":{\"Komma_und°2\":{\"curlyName\":\"Komma_und°2\",\"line\":0}}},\"Komma_und3\":{\"curlyName\":\"Komma_und3\",\"line\":0},\"entladen\":{\"curlyName\":\"entladen\",\"line\":0},\"Höhe_Höhenlage\":{\"curlyName\":\"Höhe_Höhenlage\",\"line\":2,\"args\":{\"Höhe_m\":{\"curlyName\":\"Höhe_m\",\"line\":20}}}}},{\"curlyName\":\"Gleitschnee01\",\"line\":0,\"args\":{\"Gleitschnee01§Zonen_unterhalb\":{\"curlyName\":\"Gleitschnee01§Zonen_unterhalb\",\"line\":4}}},{\"curlyName\":\"Gleitschnee01\",\"line\":0,\"args\":{\"Gleitschnee01§Zonen_unterhalb\":{\"curlyName\":\"Gleitschnee01§Zonen_unterhalb\",\"line\":2}}}", result);
	}
}
