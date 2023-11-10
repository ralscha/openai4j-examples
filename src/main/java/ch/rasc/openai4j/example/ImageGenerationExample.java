package ch.rasc.openai4j.example;

import ch.rasc.openai4j.OpenAIClient;
import ch.rasc.openai4j.images.ImageGenerationRequest;
import ch.rasc.openai4j.images.ImageGenerationRequest.Size;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

public class ImageGenerationExample {

	public static void main(String[] args) throws IOException, InterruptedException {
		String apiKey = Util.getApiKey();
		var client = OpenAIClient.create(c -> c.apiKey(apiKey));

		String input = "A bunch of people are standing in a field. They are wearing colorful clothes and holding"
				+ " umbrellas.";
		var response = client.images
				.generate(r -> r.model(ImageGenerationRequest.Model.DALL_E_3)
						.quality(ImageGenerationRequest.Quality.HD).prompt(input)
						.style(ImageGenerationRequest.Style.NATURAL).size(Size.S_1024));
		var url = response.data().get(0).url();

		try (var httpClient = HttpClient.newHttpClient()) {
			var request = HttpRequest.newBuilder().uri(URI.create(url)).build();
			var resp = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
			var fileName = "image1.png";
			try (var body = resp.body()) {
				Files.copy(body, Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
			}
		}

		String input2 = "A couple of cats are sitting on a couch.";
		response = client.images
				.generate(r -> r.model(ImageGenerationRequest.Model.DALL_E_3)
						.quality(ImageGenerationRequest.Quality.HD).prompt(input2)
						.style(ImageGenerationRequest.Style.NATURAL)
						.responseFormat(ImageGenerationRequest.ResponseFormat.B64_JSON)
						.size(Size.S_1024));
		String b64Json = response.data().get(0).b64Json();
		byte[] decodedBytes = Base64.getDecoder().decode(b64Json);
		Files.write(Paths.get("image2.png"), decodedBytes);
	}
}
