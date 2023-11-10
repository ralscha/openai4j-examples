package ch.rasc.openai4j.example;

import ch.rasc.openai4j.OpenAIClient;
import ch.rasc.openai4j.chatcompletions.SystemMessage;
import ch.rasc.openai4j.chatcompletions.UserMessage;

public class ChatCompletionsExample {
	public static void main(String[] args) {
		String apiKey = Util.getApiKey();
		var client = OpenAIClient.create(c -> c.apiKey(apiKey));

		var response = client.chatCompletions
				.create(r -> r.addMessage(SystemMessage.of("You are a helpful assistant"))
						.addMessage(UserMessage.of("What is the capital of Spain?"))
						.model("gpt-4-1106-preview"));
		System.out.println(response.choices()[0].message().content());

		/*
		 * var azureClient = OpenAIClient
		 * .create(Configuration.builder().apiVersion("2023-07-01-preview")
		 * .apiKey("...").azureDeployment("gpt-35-turbo")
		 * .azureEndpoint("https://myresource.openai.azure.com/").build()); var request =
		 * ChatCompletionsCreateRequest.builder()
		 * .addMessage(SystemMessage.of("You are a helpful assistant"))
		 * .addMessage(UserMessage.of("What is the capital of Spain?"))
		 * .model("gpt-35-turbo").build(); response =
		 * azureClient.chatCompletions.create(request); System.out.println(response);
		 */
	}

}
