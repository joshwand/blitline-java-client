package com.blitline.image.functions;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.blitline.image.AzureLocation;
import com.blitline.image.Blitline;
import com.blitline.image.BlitlineImageJob;
import com.blitline.image.BlitlineTestUtils;
import com.blitline.image.S3Location;
import com.blitline.image.SavedImage;
import com.blitline.image.functions.params.FontStyle;
import com.blitline.image.functions.params.Gravity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestSerializer {

	private static ObjectMapper mapper;

	@BeforeClass
	public static void initMapper() {
		mapper = BlitlineTestUtils.buildMapper();
	}

	@Test
	@Ignore
	// FIXME
	public void testJackson() throws JsonProcessingException {
		List<AbstractFunction> functions;

		Annotate an = new Annotate("sample text").fontFamily("Comic Sans").xOffset(120).fontStyle(FontStyle.ITALIC);
		AutoLevel al = new AutoLevel();

		functions = Arrays.asList(an, al);

		String json = mapper.writeValueAsString(functions);
		System.out.println(json);

		SavedImage save = SavedImage.withId("abcd1234random").withMetadata()
			.toS3(S3Location.of("my-bucket-name", "abcd1234random.jpg"));
		System.out.println(mapper.writeValueAsString(save));

		save = SavedImage.withId("4321nonrandom").withQuality(90).toAzure(AzureLocation.of("myAccount", "http://gobbledygook"));
		System.out.println(mapper.writeValueAsString(save));

		an.thenApply(al.andSaveResultTo(save));
		System.out.println(mapper.writeValueAsString(an));
	}

	@Test @Ignore
	// FIXME
	public void integrationText() throws JsonProcessingException {
		final String targetBucket = "mytargetbucket";
		final String applicationId = "myAppId";

		BlitlineImageJob j = BlitlineImageJob.forApplication(applicationId)
			.fromUrl("http://cdn.blitline.com/filters/boys.jpeg")
			.apply(
				new ResizeToFit(640, 480).doNotUpscale().thenApply(
					new Grayscale().andSaveResultTo(SavedImage.withId("foobar-gray").toS3(targetBucket, "grayscale.jpg")),
					new Vignette().andSaveResultTo(SavedImage.withId("foobar-vignette").toBlitlineContainer())
					)
			);

		System.out.println(mapper.writeValueAsString(j));
	}

	@Test //@Ignore
	public void twiddleErrors() throws JsonProcessingException {
		final String applicationId = "twiddleErrors";
		BlitlineImageJob j = BlitlineImageJob.forApplication(applicationId)
			.fromUrl("http://cdn.blitline.com/filters/boys.jpeg")
			.withPostback("http://gyahhowtdy.localtunnel.me/postback")
			.apply(
				Blitline.noOp().thenApply(Blitline.drawEllipse(150, 150, 200, 100).andSaveResultTo(SavedImage.withId("SUCCESS").withExifHeader("exifHeader", 42).toBlitlineContainer())),
//				Blitline.noOp().thenApply(Blitline.drawEllipse(150, 150, 200, 100).andSaveResult("FAILURE")),
				Blitline.cropToSquare().gravity(Gravity.NORTH).andSaveResult("cropsquare")
			);

		System.out.println(mapper.writeValueAsString(j));
	}

	@SuppressWarnings("rawtypes")
	@Test @Ignore
	public void des() throws IOException {
		final String test = "{\"results\":\"{\\\"original_meta\\\":null,\\\"images\\\":[{\\\"image_identifier\\\":\\\"SUCCESS\\\",\\\"s3_url\\\":\\\"http://s3.amazonaws.com/blitline/2014031018/3155/1mInTKnqiw8FiqeymvNY46A.jpg\\\",\\\"meta\\\":{\\\"width\\\":720,\\\"height\\\":540}},{\\\"error\\\":\\\"Image processing failed. Ellipse method must have origin_x,origin_y,width,height values\\\"},{\\\"failed_image_identifiers\\\":[\\\"SUCCESS\\\",\\\"FAILURE\\\"]}],\\\"job_id\\\":\\\"2uWXnYkcmfFBjQMgLyVHFrQ\\\"}\"}";

		Map r = mapper.readValue((String) mapper.readValue(test, Map.class).get("results"),Map.class);
		System.out.println(r);
	}
}
