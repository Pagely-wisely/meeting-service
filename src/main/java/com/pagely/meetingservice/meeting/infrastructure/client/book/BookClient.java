package com.pagely.meetingservice.meeting.infrastructure.client.book;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "bookservice",
        url = "${book-service.url}",
        path = "/internal/books",
        configuration = BookFeignConfig.class
)
public interface BookClient {

    @GetMapping("/{bookId}")
    BookApiResponse getBook(@PathVariable String bookId);
}