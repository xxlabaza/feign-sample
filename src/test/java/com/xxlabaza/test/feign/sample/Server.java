/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xxlabaza.test.feign.sample;

import static java.util.Comparator.comparing;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;

import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
class Server {

  final static String TOKEN = "123";

  AtomicInteger idCounter = new AtomicInteger(100);

  Map<Integer, User> repository = new ConcurrentHashMap<>();

  @PostConstruct
  void postConstruct () {
    refresh();
  }

  @PostMapping("/user")
  User create (User user) {
    val id = idCounter.getAndIncrement();
    val result = user.withId(id);
    repository.put(id, result);
    return result;
  }

  @GetMapping("/users")
  List<User> getAll (@RequestParam(name = "name", required = false) String name) {
    val byName = (Predicate<User>) user -> {
      return name == null || name.equals(user.getName());
    };
    return repository.values()
        .stream()
        .filter(byName)
        .sorted(comparing(User::getId))
        .collect(toList());
  }

  @GetMapping("/user/{id}")
  User get (@PathVariable("id") int id) {
    return repository.get(id);
  }

  @PatchMapping("/user/{id}")
  @ResponseStatus(NO_CONTENT)
  ResponseEntity<Void> patch (@PathVariable("id") int id, @RequestBody User updates) {
    val user = repository.get(id);
    if (user == null) {
      return ResponseEntity.notFound().build();
    }

    ofNullable(updates.getName())
        .ifPresent(user::setName);
    ofNullable(updates.getEmail())
        .ifPresent(user::setEmail);

    return ResponseEntity.noContent()
        .header("Patch-Id", "7")
        .build();
  }

  @DeleteMapping("/user/{id}")
  ResponseEntity<Void> delete (@PathVariable("id") int id, @RequestHeader("Authorization") String token) {
    if (!TOKEN.equals(token)) {
      return ResponseEntity.badRequest().build();
    }
    repository.remove(id);
    return ResponseEntity.noContent().build();
  }

  void refresh () {
    repository.clear();
    Stream.of(
      new User().withId(0).withName("admin").withEmail("admin@mail.ru"),
      new User().withId(1).withName("user").withEmail("user@mail.ru"),
      new User().withId(2).withName("Sergey").withEmail("guest@mail.ru")
    ).forEach(it -> repository.put(it.getId(), it));
  }

  boolean hasUser (int id) {
    return repository.containsKey(id);
  }

  boolean isEmpty () {
    return repository.isEmpty();
  }
}
