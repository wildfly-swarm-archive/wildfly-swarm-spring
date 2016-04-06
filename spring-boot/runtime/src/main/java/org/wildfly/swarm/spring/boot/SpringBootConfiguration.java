/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.spring.boot;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.wildfly.swarm.container.JARArchive;
import org.wildfly.swarm.container.runtime.AbstractServerConfiguration;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Ken Finnigan
 */
public class SpringBootConfiguration extends AbstractServerConfiguration<SpringBootFraction> {
    public SpringBootConfiguration() {
        super(SpringBootFraction.class);
    }

    @Override
    public SpringBootFraction defaultFraction() {
        return new SpringBootFraction();
    }

    @Override
    public void prepareArchive(Archive<?> archive) {
        if (JARArchive.class.isAssignableFrom(archive.getClass())) {
            // Nothing yet
        } else if (WARArchive.class.isAssignableFrom(archive.getClass())) {
            // Check for class with @SpringBootApplication.
            Map<ArchivePath, Node> content = archive.getContent();

            boolean springBootApplicationFound = false;
            Node node = null;
            Asset asset = null;

            for (Map.Entry<ArchivePath, Node> entry : content.entrySet()) {
                node = entry.getValue();
                asset = node.getAsset();
                if (hasSpringBootApplicationAnnotation(node.getPath(), asset)) {
                    springBootApplicationFound = true;
                    break;
                }
            }

            if (springBootApplicationFound) {
                // Ensure that class with @SpringBootApplication implements SpringBootServletInitializer.
                // If not, we add it.

                try (InputStream in = asset.openStream()) {
                    ClassReader reader = new ClassReader(in);
                    boolean interfaceFound = Arrays.stream(reader.getInterfaces())
                            .filter(i -> i.equals("org.springframework.boot.context.web.SpringBootServletInitializer"))
                            .count() == 1;

                    if (!interfaceFound) {
                        ClassWriter writer = new ClassWriter(0);
                        SpringBootServletInitializerAdapter sba = new SpringBootServletInitializerAdapter(writer);
                        reader.accept(, 0);
                    }
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }
            } else {
                // Add a class that implements SpringBootServletInitializer
            }
        }
    }

    private boolean hasSpringBootApplicationAnnotation(ArchivePath path, Asset asset) {
        if (asset == null) {
            return false;
        }

        if (!path.get().endsWith(".class")) {
            return false;
        }

        try (InputStream in = asset.openStream()) {
            ClassReader reader = new ClassReader(in);
            SpringBootApplicationClassVisitor visitor = new SpringBootApplicationClassVisitor();
            reader.accept(visitor, 0);
            return visitor.isFound();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        return false;
    }
}
