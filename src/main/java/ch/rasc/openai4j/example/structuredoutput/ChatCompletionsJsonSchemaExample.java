package ch.rasc.openai4j.example.structuredoutput;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.rasc.openai4j.OpenAIClient;
import ch.rasc.openai4j.chatcompletions.SystemMessage;
import ch.rasc.openai4j.chatcompletions.UserMessage;
import ch.rasc.openai4j.common.JsonSchemaService;
import ch.rasc.openai4j.example.Util;

public class ChatCompletionsJsonSchemaExample {

	record CapitalResponse(@JsonProperty(
			required = true) @JsonPropertyDescription("name of capital") String capital) {
	}

	public static void main(String[] args)
			throws JsonMappingException, JsonProcessingException {
		String apiKey = Util.getApiKey();
		var client = OpenAIClient.create(c -> c.apiKey(apiKey));

		JsonSchemaService jsonSchemaService = new JsonSchemaService();

		var response = client.chatCompletions.create(r -> r
				.addMessages(SystemMessage.of("You are a helpful assistant"),
						UserMessage.of("What is the capital of Spain?"))
				.responseFormat(jsonSchemaService
						.createStrictResponseFormat(CapitalResponse.class))
				.model("gpt-4o-mini"));
		String content = response.choices().get(0).message().content();
		System.out.println(content);

		ObjectMapper objectMapper = new ObjectMapper();
		CapitalResponse capitalResponse = objectMapper.readValue(content,
				CapitalResponse.class);
		System.out.println(capitalResponse.capital());

	}
}
