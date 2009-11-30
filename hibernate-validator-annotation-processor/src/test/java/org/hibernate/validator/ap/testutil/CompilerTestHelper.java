// $Id: CompilerTestHelper.java 17946 2009-11-06 18:23:48Z hardy.ferentschik $
/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.ap.testutil;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.Processor;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaCompiler.CompilationTask;

import org.hibernate.validator.ap.util.DiagnosticExpection;

/**
 * Infrastructure for unit tests based on the Java Compiler API.
 * 
 * @author Gunnar Morling
 *
 */
public class CompilerTestHelper {

	private final JavaCompiler compiler;
	
	private final String sourceBaseDir;

	/**
	 * TODO GM: How can JavaCompiler access all dependencies of the project? This works within
	 * Eclipse, but not on the command line.
	 */
	private final String pathToBeanValidationApiJar;
	
	public CompilerTestHelper(JavaCompiler compiler, String sourceBaseDir, String pathToBeanValidationApiJar) {

		this.compiler = compiler;
		this.sourceBaseDir = sourceBaseDir;
		this.pathToBeanValidationApiJar = pathToBeanValidationApiJar;
	}

	public File getSourceFile(Class<?> clazz) {
		
		String sourceFileName =
			File.separator + clazz.getName().replace(".", File.separator) + ".java";
		
		return new File(sourceBaseDir + sourceFileName);
	}
	
	public boolean compile(
		Processor annotationProcessor, DiagnosticCollector<JavaFileObject> diagnostics, File... sourceFiles) {
		
		StandardJavaFileManager fileManager = 
			compiler.getStandardFileManager(null, null, null);
		
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(sourceFiles);
		List<String> optionList = new ArrayList<String>();
		optionList.addAll(Arrays.asList("-classpath", pathToBeanValidationApiJar));

		CompilationTask task = compiler.getTask(null, fileManager, diagnostics, optionList, null, compilationUnits);

		task.setProcessors(Arrays.asList(annotationProcessor));
		
		return task.call();
	}
	
	public static void assertDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics, DiagnosticExpection... expections) {
		
		if(expections == null) {
			assertTrue(diagnostics.getDiagnostics().isEmpty());
		}
		else {
			
			assertEquals(diagnostics.getDiagnostics().size(), expections.length);
			
			int i = 0;
			for (DiagnosticExpection oneExpection : expections) {
				
				assertEquals(diagnostics.getDiagnostics().get(i).getKind(), oneExpection.getKind());
				assertEquals(diagnostics.getDiagnostics().get(i).getLineNumber(), oneExpection.getLineNumber());
			}
		}
	}
	
}
