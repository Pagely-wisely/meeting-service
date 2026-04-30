package com.pagely.meetingservice.meeting.application.port;

// Meeting Service에서 Book Service를 추상화해서 사용하기 위한 Port
public interface BookProvider {

    // bookId가 유효한 책인지 확인한다.
    void validateBook(String bookId);
}