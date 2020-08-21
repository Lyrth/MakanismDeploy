/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext

import static groovy.lang.Closure.IDENTITY

/**
 * Modified to allow property values transformation
 *
 * {@inheritDoc}
 */
class PropertiesValuesTransformer extends PropertiesFileTransformer {

    Closure<String> valueTransformer = IDENTITY

    @Override
    void transform(TransformerContext context) {
        Properties props = propertiesEntries[context.path]
        Properties incoming = loadAndTransformKV(context.is)
        if (props == null) {
            propertiesEntries[context.path] = incoming
        } else {
            incoming.each { key, value ->
                if (props.containsKey(key)) {
                    switch (mergeStrategyFor(context.path).toLowerCase()) {
                        case 'latest':
                            props.put(key, value)
                            break
                        case 'append':
                            props.put(key, props.getProperty(key) + mergeSeparatorFor(context.path) + value)
                            break
                        case 'first':
                        default:
                            // continue
                            break
                    }
                } else {
                    props.put(key, value)
                }
            }
        }
    }

    private Properties loadAndTransformKV(InputStream is) {
        Properties props = new Properties()
        props.load(is)
        return transformKV(props)
    }

    private Properties transformKV(Properties properties) {
        if (keyTransformer == IDENTITY && valueTransformer == IDENTITY)
            return properties
        def result = new Properties()
        properties.each { key, value ->
            result.put(keyTransformer.call(key), valueTransformer.call(value))
        }
        return result
    }

    private String mergeStrategyFor(String path) {
        if (mappings.containsKey(path)) {
            return mappings.get(path).mergeStrategy ?: mergeStrategy
        }
        for (key in mappings.keySet()) {
            if (path =~ /$key/) {
                return mappings.get(key).mergeStrategy ?: mergeStrategy
            }
        }

        return mergeStrategy
    }

    private String mergeSeparatorFor(String path) {
        if (mappings.containsKey(path)) {
            return mappings.get(path).mergeSeparator ?: mergeSeparator
        }
        for (key in mappings.keySet()) {
            if (path =~ /$key/) {
                return mappings.get(key).mergeSeparator ?: mergeSeparator
            }
        }

        return mergeSeparator
    }
}
