/*
 * Copyright 2017 Dennis Vriend
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

package hello

case class Person(id: Long, name: String, age: Int)

object PersonService {
  def apply(): PersonService =
    new InMemoryPersonService
}

trait PersonService {
  def find(id: Long): Option[Person]
  def add(person: Person): Unit
}

private[hello] class InMemoryPersonService extends PersonService {
  var map: Map[Long, Person] = Map.empty
  override def find(id: Long): Option[Person] = map.get(id)

  override def add(person: Person): Unit = map += person.id -> person
}
