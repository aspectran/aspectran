/**
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.console.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * <p>Created: 2017. 3. 5.</p>
 */
public class CommandParserTest {

	@Test
	public void testRedirectionOperators() {
		List<RedirectionOperation> list = CommandParser.findAllRedirectionOperators(">> abcde > 12345");
		Assert.assertEquals(list.get(0).getRedirectionOperator(), RedirectionOperator.APPEND_OUT);
		Assert.assertEquals(list.get(0).getBuffer(), "abcde");
		Assert.assertEquals(list.get(1).getRedirectionOperator(), RedirectionOperator.OVERWRITE_OUT);
		Assert.assertEquals(list.get(1).getBuffer(), "12345");
	}

	@Test
	public void testRedirectionOperators2() {
		List<RedirectionOperation> list = CommandParser.findAllRedirectionOperators("> '<abcde>' >> 12345");
		Assert.assertEquals(list.get(0).getRedirectionOperator(), RedirectionOperator.OVERWRITE_OUT);
		Assert.assertEquals(list.get(0).getBuffer(), "'<abcde>'");
		Assert.assertEquals(list.get(1).getRedirectionOperator(), RedirectionOperator.APPEND_OUT);
		Assert.assertEquals(list.get(1).getBuffer(), "12345");
	}

}
