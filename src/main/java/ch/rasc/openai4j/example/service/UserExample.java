package ch.rasc.openai4j.example.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rasc.openai4j.OpenAIClient;
import ch.rasc.openai4j.chatcompletions.ChatCompletionsService;
import ch.rasc.openai4j.chatcompletions.ChatCompletionsService.Mode;
import ch.rasc.openai4j.chatcompletions.UserMessage;
import ch.rasc.openai4j.example.Util;

public class UserExample {

	record User(
			@JsonProperty(
					required = true) @JsonPropertyDescription("Age of a user") int age,
			@JsonProperty(
					required = true) @JsonPropertyDescription("Name of a user") String name,

			@JsonPropertyDescription("Role of a user. Optional") String role) {
	}

	public static void main(String[] args) {
		String apiKey = Util.getApiKey();
		var client = OpenAIClient.create(c -> c.apiKey(apiKey));
		ObjectMapper om = new ObjectMapper();
		var service = new ChatCompletionsService(client.chatCompletions, om);

		var response = service.create(r -> r
				.addMessages(
						UserMessage.of("Get user details for: Jason is 25 years old"))
				.model("gpt-4-1106-preview"), User.class, Mode.JSON, 2);
		System.out.println(response.responseModel());
		System.out.println(response.response());

		System.out.println("=======");

		response = service.create(r -> r
				.addMessages(
						UserMessage.of("Get user details for: Jason is 25 years old"))
				.model("gpt-4-1106-preview"), User.class, Mode.TOOL, 2);
		System.out.println(response.responseModel());
		System.out.println(response.response());

		System.out.println("=======");

		response = service.create(
				r -> r.addMessages(UserMessage.of("Jason is a 25 years old scientist"))
						.model("gpt-3.5-turbo-1106"),
				User.class, Mode.TOOL, 2);
		System.out.println(response.responseModel());
		System.out.println(response.response());

		System.out.println("=======");

		response = service.create(
				r -> r.addMessages(UserMessage.of("Jason is a 25 years old scientist"))
						.model("gpt-3.5-turbo-1106"),
				User.class, Mode.JSON, 2);
		System.out.println(response.responseModel());
		System.out.println(response.response());
	}

}
