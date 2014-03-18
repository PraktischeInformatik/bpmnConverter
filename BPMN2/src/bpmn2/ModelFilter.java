package bpmn2;

import java.io.File;
import java.io.IOException;
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

import de.uni_potsdam.hpi.bpt.ai.diagram.Diagram;
import de.uni_potsdam.hpi.bpt.ai.diagram.Shape;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.Representation;
import de.uni_potsdam.hpi.bpt.promnicat.util.IllegalTypeException;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.transformer.BpmaiJsonToDiagramUnit;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitData;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitDataJbpt;

public class ModelFilter {

	Path path;
	int minRevs;
	int minElements;
	int maxElements;
	int minDifferentElements;

	int totalModelCount = 0;
	int revisionCount = 1;
	Path lastRevisionPath = Paths.get("");

	// contains Path to History and, in this order, its: number of revisions,
	// minElements,
	// maxElements, avgElements
	HashMap<String, int[]> historyDescriptions = new HashMap<String, int[]>();

	ModelFilter(Path path, int minRevs, int minElements, int maxElements, int minDifferentElements) throws IOException {
		this.path = path;
		this.minRevs = minRevs;
		this.minElements = minElements;
		this.maxElements = maxElements;
		this.minDifferentElements = minDifferentElements;
	}

	public Path createFilteredFile(boolean useExistingFile, boolean getAllRevisions) throws IOException, IllegalTypeException {
		if (useExistingFile) {
			Path returnPath;
			if (getAllRevisions) {
				returnPath = noRevisionfilter(ignoreMinDifferentElements(irgnoreMinMaxElementModels(Paths.get("ModelsMinRevs" + minRevs + ".txt"))));
			} else {
				returnPath = ignoreMinDifferentElements(irgnoreMinMaxElementModels(Paths.get("ModelsMinRevs" + minRevs + ".txt")));
			}
			analyseFile(returnPath, true);
			return returnPath;
		} else {
			Path returnPath;
			if (getAllRevisions) {
				returnPath = noRevisionfilter(ignoreMinDifferentElements(irgnoreMinMaxElementModels(ignoreMinRevModels())));
			} else {
				returnPath = ignoreMinDifferentElements(irgnoreMinMaxElementModels(ignoreMinRevModels()));
			}
			analyseFile(returnPath, false);
			return returnPath;
		}
	}

	private Path ignoreMinRevModels() throws IOException {
		System.out.println("Rev Filter started: MinRevs=" + minRevs);
		final Path revPaths = Paths.get("ModelsMinRevs" + minRevs + ".txt");
		Files.write(revPaths, "".getBytes());
		Files.walkFileTree(Paths.get(this.path.toUri()), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) {
				return FileVisitResult.SKIP_SUBTREE;
			}

			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attribs) {
				try {
					if (path.getFileName().toString().endsWith(".json") && !path.getFileName().toString().endsWith("_metadata.json")) {
						totalModelCount += 1;
						if (revisionCount == minRevs) {
							Files.write(Paths.get(revPaths.toUri()), ((lastRevisionPath + System.getProperty("line.separator")).getBytes()),
									StandardOpenOption.APPEND);
						}
						if (lastRevisionPath.equals(Paths.get(path.toString().substring(0, path.toString().lastIndexOf(File.separatorChar))))) {
							revisionCount += 1;
						} else {
							revisionCount = 1;
						}
						lastRevisionPath = Paths.get(path.toString().substring(0, path.toString().lastIndexOf(File.separatorChar)));
					}

				} catch (Exception e) {
				}

				return FileVisitResult.CONTINUE;
			}
		});
		System.out.println("Rev Filter completed");
		return revPaths;
	}

	private Path irgnoreMinMaxElementModels(Path path) throws IllegalTypeException, IOException {
		System.out.println("MinMax Filter started: Min=" + minElements + ", Max=" + maxElements);
		List<String> modelPaths = Files.readAllLines(path, Charset.defaultCharset());
		final Path revPaths = Paths.get("ModelsMinRevs" + minRevs + "ModelsMinElements" + minElements + "ModelMaxElements" + maxElements + ".txt");
		Files.write(revPaths, "".getBytes());
		for (String modelPath : modelPaths) {
			Files.walkFileTree(Paths.get(modelPath), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					return FileVisitResult.SKIP_SUBTREE;
				}

				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attribs) throws IOException {
					try {
						if (path.getFileName().toString().endsWith(".json") && !path.getFileName().toString().endsWith("_metadata.json")) {
							Diagram diagram = jsonToDiagram(path.toString());
							if (minElements <= diagram.getShapes().size() && diagram.getShapes().size() <= maxElements) {
								Files.write(Paths.get(revPaths.toUri()), ((path + System.getProperty("line.separator")).getBytes()), StandardOpenOption.APPEND);
							}
						}
					} catch (IllegalTypeException e) {
						e.printStackTrace();
					} catch (RuntimeException re) {
						System.out.println("Could not transfrom: " + path);
						return FileVisitResult.CONTINUE;
					}

					return FileVisitResult.CONTINUE;
				}
			});
		}
		System.out.println("MinMax Filter completed");
		return revPaths;
	}

	private Path ignoreMinDifferentElements(Path path) throws IOException {
		System.out.println("DiffElements Filter started: minDifferentElements=" + minDifferentElements);
		List<String> modelPaths = Files.readAllLines(path, Charset.defaultCharset());
		final Path revPaths = Paths.get("ModelsMinRevs" + minRevs + "ModelsMinElements" + minElements + "ModelsMaxElements" + maxElements
				+ "ModelsMinDifferentElements" + minDifferentElements + ".txt");
		Files.write(revPaths, "".getBytes());
		for (String modelPath : modelPaths) {
			Files.walkFileTree(Paths.get(modelPath), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					return FileVisitResult.SKIP_SUBTREE;
				}

				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attribs) throws IOException {
					try {
						if (path.getFileName().toString().endsWith(".json") && !path.getFileName().toString().endsWith("_metadata.json")) {
							Diagram diagram = jsonToDiagram(path.toString());
							int stencilcount = 0;
							ArrayList<String> shapeIds = new ArrayList<String>();
							for (Shape shape : diagram.getShapes()) {
								if (!shapeIds.contains(shape.getStencilId())) {
									stencilcount += 1;
									shapeIds.add(shape.getStencilId());
								}
							}
							if (stencilcount >= minDifferentElements)
								Files.write(Paths.get(revPaths.toUri()), ((path + System.getProperty("line.separator")).getBytes()), StandardOpenOption.APPEND);
						}
					} catch (IllegalTypeException e) {
						e.printStackTrace();
					} catch (RuntimeException re) {
						System.out.println("Could not transfrom: " + path);
						return FileVisitResult.CONTINUE;
					}

					return FileVisitResult.CONTINUE;
				}
			});
		}
		System.out.println("DiffElements Filter completed");
		return revPaths;
	}

	private Diagram jsonToDiagram(String path) throws IllegalTypeException {
		BpmaiJsonToDiagramUnit unit = new BpmaiJsonToDiagramUnit();
		Representation representation = new Representation();
		File file = new File(path);
		representation.importFile(file);
		IUnitData<Object> input = new UnitDataJbpt<Object>(representation);
		Diagram diagram = (Diagram) unit.execute(input).getValue();
		return diagram;
	}

	private Path noRevisionfilter(Path path) throws IOException {
		List<String> modelPaths = Files.readAllLines(path, Charset.defaultCharset());
		final Path revPaths = Paths.get(path + "RevisionsRestored.txt");
		Files.write(revPaths, "".getBytes());
		ArrayList<String> parentFolders = new ArrayList<String>();
		String parentFolder;
		for (String modelPath : modelPaths) {
			parentFolder = modelPath.substring(0, modelPath.toString().lastIndexOf(File.separatorChar));
			if (!parentFolders.contains(parentFolder)) {
				parentFolders.add(parentFolder);
				Files.write(Paths.get(revPaths.toUri()), ((parentFolder + System.getProperty("line.separator")).getBytes()), StandardOpenOption.APPEND);
			}
		}
		return revPaths;
	}

	/**
	 * 
	 * @param path
	 *            to file which contains model paths
	 * @return
	 * @throws IOException
	 */
	public int countAllRevisions(Path path) throws IOException {
		List<String> modelPaths = Files.readAllLines(path, Charset.defaultCharset());
		revisionCount = 0;
		for (String modelPath : modelPaths) {
			Files.walkFileTree(Paths.get(modelPath), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					return FileVisitResult.SKIP_SUBTREE;
				}

				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attribs) throws IOException {
					if (path.getFileName().toString().endsWith(".json") && !path.getFileName().toString().endsWith("_metadata.json")) {
						revisionCount += 1;
					}

					return FileVisitResult.CONTINUE;
				}
			});
		}
		return revisionCount;
	}

	private void analyseFile(Path path, boolean fromExistingFile) throws IOException {
		Path analyseFile = Paths.get(path.toString().substring(0, path.toString().indexOf(".")) + "Analyse.txt");
		if (fromExistingFile)
			Files.write(
					analyseFile,
					("Total number of models before filtering (from revision filtered file): "
							+ countAllRevisions(Paths.get("ModelsMinRevs" + minRevs + ".txt")) + System.getProperty("line.separator")).getBytes());
		else
			Files.write(analyseFile, ("Total number of models before filtering: " + totalModelCount + System.getProperty("line.separator")).getBytes());

		Files.write(analyseFile, ("Filter properties:" + System.getProperty("line.separator")).getBytes(), StandardOpenOption.APPEND);
		Files.write(analyseFile, ("Minumum Revisions: " + minRevs + System.getProperty("line.separator")).getBytes(), StandardOpenOption.APPEND);
		Files.write(analyseFile, ("Minumum BPMN Elements: " + minElements + System.getProperty("line.separator")).getBytes(), StandardOpenOption.APPEND);
		Files.write(analyseFile, ("Maximum BPMN Elements: " + maxElements + System.getProperty("line.separator")).getBytes(), StandardOpenOption.APPEND);
		Files.write(analyseFile, ("Minumum different BPMN Elements: " + minDifferentElements + System.getProperty("line.separator") + System
				.getProperty("line.separator")).getBytes(), StandardOpenOption.APPEND);

		HashMap<String, int[]> historyInformation = getHistoryInformation(path);
		int globalMinElements = 999999999; // random high value
		int globalMaxElements = 0;
		int globalAvgElements = 0;
		int avgAdded = 0;
		int totalNumberOfModels = countAllRevisions(path);
		for (int[] historyInfo : historyDescriptions.values()) {
			if (historyInfo[1] < globalMinElements) {
				globalMinElements = historyInfo[1];
			}
			if (historyInfo[2] > globalMinElements) {
				globalMaxElements = historyInfo[2];
			}
			avgAdded += historyInfo[3]*historyInfo[0];
		}
		if (totalNumberOfModels > 0)
			globalAvgElements = avgAdded / totalNumberOfModels;
		Files.write(analyseFile, ("Global Values:" + System.getProperty("line.separator")).getBytes(), StandardOpenOption.APPEND);
		Files.write(analyseFile, ("Minumum Elements: " + globalMinElements + System.getProperty("line.separator")).getBytes(), StandardOpenOption.APPEND);
		Files.write(analyseFile, ("Maximum Elements: " + globalMaxElements + System.getProperty("line.separator")).getBytes(), StandardOpenOption.APPEND);
		Files.write(analyseFile,
				("Average Elements: " + globalAvgElements + System.getProperty("line.separator") + System.getProperty("line.separator")).getBytes(),
				StandardOpenOption.APPEND);

		Files.write(analyseFile, ("Number of Histories: " + historyDescriptions.size() + System.getProperty("line.separator")).getBytes(),
				StandardOpenOption.APPEND);
		Files.write(analyseFile,
				("Number of Models: " + totalNumberOfModels + System.getProperty("line.separator") + System.getProperty("line.separator")).getBytes(),
				StandardOpenOption.APPEND);

		for (String key : historyInformation.keySet()) {
			Files.write(
					analyseFile,
					(key + " : " + System.getProperty("line.separator") + "Revisions: " + historyDescriptions.get(key)[0]
							+ System.getProperty("line.separator") + "minElements: " + historyDescriptions.get(key)[1] + System.getProperty("line.separator")
							+ "maxElements: " + historyDescriptions.get(key)[2] + System.getProperty("line.separator") + "avgElements: "
							+ historyDescriptions.get(key)[3] + System.getProperty("line.separator")).getBytes(), StandardOpenOption.APPEND);
		}
	}

	/**
	 * 
	 * @param path
	 *            to file which contains model paths
	 * @return historyDescriptions after execution the variable contains the
	 *         ElementInformation for all Histories contained in the given file.
	 * @throws IOException
	 */
	private HashMap<String, int[]> getHistoryInformation(Path path) throws IOException {
		List<String> modelPaths = Files.readAllLines(path, Charset.defaultCharset());
		for (String modelPath : modelPaths) {
			Files.walkFileTree(Paths.get(modelPath), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					return FileVisitResult.SKIP_SUBTREE;
				}

				@Override
				public FileVisitResult visitFile(Path path, BasicFileAttributes attribs) throws IOException {
					if (path.getFileName().toString().endsWith(".json") && !path.getFileName().toString().endsWith("_metadata.json")) {
						try {
							Diagram diagram = jsonToDiagram(path.toString());

							if (historyDescriptions.containsKey(path.toString().substring(0, path.toString().lastIndexOf(File.separatorChar)))) {

								historyDescriptions.get(path.toString().substring(0, path.toString().lastIndexOf(File.separatorChar)))[0] = historyDescriptions
										.get(path.toString().substring(0, path.toString().lastIndexOf(File.separatorChar)))[0] + 1;

								if (diagram.getShapes().size() < historyDescriptions.get(path.toString().substring(0,
										path.toString().lastIndexOf(File.separatorChar)))[1])
									historyDescriptions.get(path.toString().substring(0, path.toString().lastIndexOf(File.separatorChar)))[1] = diagram
											.getShapes().size();

								if (diagram.getShapes().size() > historyDescriptions.get(path.toString().substring(0,
										path.toString().lastIndexOf(File.separatorChar)))[2])
									historyDescriptions.get(path.toString().substring(0, path.toString().lastIndexOf(File.separatorChar)))[2] = diagram
											.getShapes().size();

								historyDescriptions.get(path.toString().substring(0, path.toString().lastIndexOf(File.separatorChar)))[3] = historyDescriptions
										.get(path.toString().substring(0, path.toString().lastIndexOf(File.separatorChar)))[3] + diagram.getShapes().size();

							} else {
								historyDescriptions.put(path.toString().substring(0, path.toString().lastIndexOf(File.separatorChar)), new int[] { 1,
										diagram.getShapes().size(), diagram.getShapes().size(), diagram.getShapes().size() });
							}
						} catch (IllegalTypeException e) {
							e.printStackTrace();
						}
					}

					return FileVisitResult.CONTINUE;
				}
			});
		}

		for (String history : historyDescriptions.keySet())
			historyDescriptions.get(history)[3] = historyDescriptions.get(history)[3] / historyDescriptions.get(history)[0];
		return historyDescriptions;
	}
}
