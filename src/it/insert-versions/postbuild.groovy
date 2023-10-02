/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

file = new File(basedir, 'target/licenses.xml');
expectedFile = new File(basedir, 'expected_licenses.xml');
assert expectedFile.text.equals(file.text);
return true;