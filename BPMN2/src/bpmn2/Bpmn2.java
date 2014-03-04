package bpmn2;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class Bpmn2 implements IApplication {

	int modelsTransformed = 0;
	int modelsFailed = 0;

	@Override
	public Object start(IApplicationContext context) throws Exception {

		Files.write(Paths.get("UnmatchedItems.txt"), "".getBytes());
		Files.write(Paths.get("IncompleteDiagrams.txt"), "".getBytes());

//		ModelFilter modelFilter = new ModelFilter(Paths.get("C:\\Users\\Chrizz\\Downloads\\eclipse-modeling-juno-SR2-win32-x86_64\\eclipse\\workspace\\promnicat\\BPMN2Models"),10,250,20);
//		modelFilter.createFilteredFile(false);
		
		// System.out.println("Revisions: "+modelFilter.countAllRevisions(modelFilter.noRevisionfilter(modelFilter.createFilteredFile(true))));
		 convertModelsByFile(Paths.get("C:\\Program Files (x86)\\eclipse\\ModelsMinRevs10ModelsMinElements250ModelsMinDifferentElements20.txtRevisionsRestored855.txt"));
//		 convertModelsByPath(Paths.get("C:\\Users\\Chrizz\\Downloads\\eclipse-modeling-juno-SR2-win32-x86_64\\eclipse\\BPMN2Models\\367593267\\BPMN2.0_Process\\2012-08-30_129589600\\129589600_rev17.json"));
//		 convertModelsByPath(Paths.get("C:\\Users\\Chrizz\\Downloads\\eclipse-modeling-juno-SR2-win32-x86_64\\eclipse\\BPMN2Models\\367593267\\BPMN2.0_Process\\2012-08-30_129589600\\129589600_rev7.json"));
		 // JsonToBpmn2Converter conv = new JsonToBpmn2Converter();
		// conv.execute("C:\\Users\\Chrizz\\Downloads\\eclipse-modeling-juno-SR2-win32-x86_64\\eclipse\\workspace\\promnicat\\BPMN2Models\\1089424718\\BPMN2.0_Process\\2012-04-16_HW1 (Arthur)\\1616816944_rev1.json");

		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	private void convertModelsByFile(Path path) throws IOException {
		List<String> modelPaths = Files.readAllLines(path, Charset.defaultCharset());
		for (String modelPath : modelPaths) {
			Files.walkFileTree(Paths.get(modelPath), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					return FileVisitResult.SKIP_SUBTREE;
				}

				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attribs) {
					try {
						if (path.getFileName().toString().endsWith(".json") && !path.getFileName().toString().endsWith("_metadata.json")) {
							System.out.println(path);
							modelsTransformed += 1;
							JsonToBpmn2Converter conv = new JsonToBpmn2Converter();
							conv.execute(path.toString());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					return FileVisitResult.CONTINUE;
				}
			});
		}
		modelsFailed = Files.readAllLines(Paths.get("IncompleteDiagrams.txt"), Charset.defaultCharset()).size();
		int correctModelsAbsolute = (modelsTransformed - modelsFailed);
		double correctModelsPercent = (((double) correctModelsAbsolute / modelsTransformed) * 100);
		System.out.println("Successfully transformed " + correctModelsAbsolute + " of " + modelsTransformed + " Models (" + Math.round(correctModelsPercent) + "%)");
	}

	public void convertModelsByPath(Path path) throws IOException {
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) {
				return FileVisitResult.SKIP_SUBTREE;
			}

			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attribs) {
				try {
					if (path.getFileName().toString().endsWith(".json") && !path.getFileName().toString().endsWith("_metadata.json")) {
						System.out.println(path);
						modelsTransformed += 1;
						JsonToBpmn2Converter conv = new JsonToBpmn2Converter();
						conv.execute(path.toString());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return FileVisitResult.CONTINUE;
			}
		});
		modelsFailed = Files.readAllLines(Paths.get("IncompleteDiagrams.txt"), Charset.defaultCharset()).size();
		int correctModelsAbsolute = (modelsTransformed - modelsFailed);
		double correctModelsPercent = (((double) correctModelsAbsolute / modelsTransformed) * 100);
		System.out.println("Successfully transformed " + correctModelsAbsolute + " of " + modelsTransformed + " Models (" + Math.round(correctModelsPercent) + "%)");
	}

}
