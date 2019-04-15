package com.hust.buidoandung.weatherapp;

public class ApiResult {
    ServerResult serverResult;
    TaskResult taskResult;
    enum ServerResult{
        OK,JSON_EXCEPTION,CITY_NOT_FOUND
    }
    enum TaskResult{
        SUCCESS, BAD_RESPONSE, IO_EXCEPTION, TOO_MANY_REQUESTS
    }
}

