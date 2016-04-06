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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author Ken Finnigan
 */
public class SpringBootApplicationClassVisitor extends ClassVisitor {
    private boolean found = false;

    public SpringBootApplicationClassVisitor() {
        super(Opcodes.ASM5);
    }

    public boolean isFound() {
        return this.found;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (desc.equals("Lorg/springframework/boot/autoconfigure/SpringBootApplication;")) {
            found = true;
        }
        return super.visitAnnotation(desc, visible);
    }
}
