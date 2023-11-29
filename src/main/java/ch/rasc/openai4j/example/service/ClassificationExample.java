package ch.rasc.openai4j.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rasc.openai4j.OpenAIClient;
import ch.rasc.openai4j.chatcompletions.ChatCompletionsService;
import ch.rasc.openai4j.chatcompletions.ChatCompletionsService.Mode;
import ch.rasc.openai4j.chatcompletions.UserMessage;
import ch.rasc.openai4j.example.Util;

public class ClassificationExample {

	enum Label {
		SPAM, NOT_SPAM
	}

	record Prediction(Label label) {
	}

	public static void main(String[] args) {
		String apiKey = Util.getApiKey();
		var client = OpenAIClient.create(c -> c.apiKey(apiKey));
		ObjectMapper om = new ObjectMapper();
		var service = new ChatCompletionsService(client.chatCompletions, om);

		var response = service.create(r -> r.addMessages(UserMessage.of(
				"Classify the following text: Hello there I'm a nigerian prince and I want to give you money"))
				.model("gpt-4-1106-preview"), Prediction.class, Mode.JSON, 2);
		System.out.println(response.responseModel());
		System.out.println(response.response());

	}

}