package ch.rasc.openai4j.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;

import ch.rasc.openai4j.OpenAIClient;
import ch.rasc.openai4j.chatcompletions.AssistantMessage;
import ch.rasc.openai4j.chatcompletions.ChatCompletionMessage;
import ch.rasc.openai4j.chatcompletions.ChatCompletionTool;
import ch.rasc.openai4j.chatcompletions.ChatCompletionsResponse.Choice.FinishReason;
import ch.rasc.openai4j.chatcompletions.SystemMessage;
import ch.rasc.openai4j.chatcompletions.ToolMessage;
import ch.rasc.openai4j.chatcompletions.UserMessage;
import ch.rasc.openai4j.common.Function;

public class ChatCompletionsFunctionExample {

	enum WeatherUnit {
		CELSIUS, FAHRENHEIT;
	}

	static class Weather {
		@JsonProperty(required = true)
		@JsonPropertyDescription("City and state, for example: Bern, Switzerland")
		public String location;

		@JsonPropertyDescription("The temperature unit, can be 'celsius' or 'fahrenheit'")
		@JsonProperty(required = true)
		public WeatherUnit unit;

		@Override
		public String toString() {
			return "Weather [location=" + location + ", unit=" + unit + "]";
		}

	}

	public static void main(String[] args)
			throws JsonMappingException, JsonProcessingException {

		String apiKey = Util.getApiKey();
		var client = OpenAIClient.create(c -> c.apiKey(apiKey));

		ObjectMapper om = new ObjectMapper();

		JacksonModule module = new JacksonModule(
				JacksonOption.RESPECT_JSONPROPERTY_REQUIRED);
		SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(
				SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON).with(module);
		SchemaGeneratorConfig config = configBuilder.build();
		SchemaGenerator generator = new SchemaGenerator(config);
		JsonNode jsonSchema = generator.generateSchema(Weather.class);

		List<ChatCompletionMessage> thread = new ArrayList<>();
		thread.add(SystemMessage.of("You are a helpful assistant"));
		thread.add(UserMessage
				.of("What is the current temperature in Sidney in Fahrenheit?"));

		var response = client.chatCompletions.create(r -> r.addAllMessages(thread)
				.addTool(ChatCompletionTool.of(Function.of("get_temperature",
						"Get the current temperature of a location", jsonSchema)))
				.model("gpt-4-1106-preview"));

		var choice = response.choices()[0];
		if (choice.finishReason() == FinishReason.TOOL_CALLS) {
			var message = choice.message();
			if (message.toolCalls() != null) {
				thread.add(AssistantMessage.of(choice.message()));
				for (var toolCall : message.toolCalls()) {
					if (toolCall.function().name().equals("get_temperature")) {
						Weather weather = om.readValue(toolCall.function().arguments(),
								Weather.class);
						System.out.println(
								"calling get_temperature with parameters:" + weather);
						thread.add(ToolMessage.of(toolCall.id(), "72"));
					}
				}
			}

			System.out.println(om.writeValueAsString(thread));

			// send it a second time
			response = client.chatCompletions
					.create(r -> r.addAllMessages(thread).model("gpt-4-1106-preview"));
			System.out.println(response);

		}
		else {
			System.out.println("Not a function call");
			System.out.println(choice);
		}

	}
}
