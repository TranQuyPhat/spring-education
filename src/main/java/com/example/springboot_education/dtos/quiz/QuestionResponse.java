package com.example.springboot_education.dtos.quiz;

import com.example.springboot_education.dtos.quiz.teacher.QuestionTeacherDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class QuestionResponse {
    @JsonProperty("questions")
    private List<QuestionTeacherDTO> questions;

    public QuestionResponse() {
    }

    public QuestionResponse(List<QuestionTeacherDTO> questions) {
        this.questions = questions;
    }

    // Getters and Setters
    public List<QuestionTeacherDTO> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionTeacherDTO> questions) {
        this.questions = questions;
    }

    public static class Question {
        @JsonProperty("questionText")
        private String questionText;

        @JsonProperty("questionType")
        private String questionType;

        @JsonProperty("options")
        private List<OptionDTO> options;

        @JsonProperty("correctAnswer")
        private String correctAnswer;

        public Question() {
        }

        public Question(String questionText, String questionType, List<OptionDTO> options, String correctAnswer) {
            this.questionText = questionText;
            this.questionType = questionType;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }

        // Getters and Setters
        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }

        public String getQuestionType() {
            return questionType;
        }

        public void setQuestionType(String questionType) {
            this.questionType = questionType;
        }

        public List<OptionDTO> getOptions() {
            return options;
        }

        public void setOptions(List<OptionDTO> options) {
            this.options = options;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public void setCorrectAnswer(String correctAnswer) {
            this.correctAnswer = correctAnswer;
        }
    }
}