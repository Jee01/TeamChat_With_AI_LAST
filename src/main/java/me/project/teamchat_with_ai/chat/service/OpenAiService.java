package me.project.teamchat_with_ai.chat.service;

import me.project.teamchat_with_ai.chat.config.GptConfig;
import org.json.JSONException;
import org.springframework.stereotype.Service;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class OpenAiService {

    private final GptConfig gptConfig;
    private final HttpClient httpClient;

    public OpenAiService(GptConfig gptConfig) {
        this.gptConfig = gptConfig;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String getCompletion(String userPrompt) throws IOException, InterruptedException, JSONException {
        String apiUrl = "https://api.openai.com/v1/chat/completions";

        // OpenAI Chat Completion 포맷에 맞게 메시지 구성
        // role: user, content: 사용자가 입력한 prompt
        JSONArray messages = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);
        messages.put(userMessage);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", gptConfig.getModel()); // ex) "gpt-3.5-turbo"
        requestBody.put("messages", messages);
        // 필요하다면 추가 파라미터 (temperature 등) 설정 가능
        // requestBody.put("temperature", 0.7);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + gptConfig.getSecretKey())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // 응답 JSON 파싱
            JSONObject responseJson = new JSONObject(response.body());
            JSONArray choices = responseJson.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject choice = choices.getJSONObject(0);
                JSONObject message = choice.getJSONObject("message");
                return message.getString("content").trim();
            } else {
                return "No response from model.";
            }
        } else {
            return "Error: " + response.statusCode() + " " + response.body();
        }
    }
}