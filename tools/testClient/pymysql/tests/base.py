# Copyright 2012 Citrix Systems, Inc. Licensed under the
# Apache License, Version 2.0 (the "License"); you may not use this
# file except in compliance with the License.  Citrix Systems, Inc.
# reserves all rights not expressly granted by the License.
# You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# 
# Automatically generated by addcopyright.py at 04/03/2012
import pymysql
import unittest

class PyMySQLTestCase(unittest.TestCase):
    # Edit this to suit your test environment.
    databases = [
        {"host":"localhost","user":"root",
         "passwd":"","db":"test_pymysql", "use_unicode": True},
        {"host":"localhost","user":"root","passwd":"","db":"test_pymysql2"}]

    def setUp(self):
        self.connections = []

        for params in self.databases:
            self.connections.append(pymysql.connect(**params))

    def tearDown(self):
        for connection in self.connections:
            connection.close()

