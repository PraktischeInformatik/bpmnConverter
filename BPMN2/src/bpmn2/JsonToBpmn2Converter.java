package bpmn2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.dd.dc.Bounds;
import org.eclipse.dd.dc.DcFactory;
import org.eclipse.dd.dc.Point;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.bpmn2.Artifact;
import org.eclipse.bpmn2.Association;
import org.eclipse.bpmn2.AssociationDirection;
import org.eclipse.bpmn2.Auditing;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.CancelEventDefinition;
import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.CompensateEventDefinition;
import org.eclipse.bpmn2.ComplexGateway;
import org.eclipse.bpmn2.ConditionalEventDefinition;
import org.eclipse.bpmn2.DataObject;
import org.eclipse.bpmn2.DataStore;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.ErrorEventDefinition;
import org.eclipse.bpmn2.EscalationEventDefinition;
import org.eclipse.bpmn2.EventBasedGateway;
import org.eclipse.bpmn2.EventBasedGatewayType;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.Expression;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Group;
import org.eclipse.bpmn2.InclusiveGateway;
import org.eclipse.bpmn2.InteractionNode;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.LaneSet;
import org.eclipse.bpmn2.LinkEventDefinition;
import org.eclipse.bpmn2.LoopCharacteristics;
import org.eclipse.bpmn2.ManualTask;
import org.eclipse.bpmn2.Message;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.MessageFlow;
import org.eclipse.bpmn2.Monitoring;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.ParticipantMultiplicity;
import org.eclipse.bpmn2.ReceiveTask;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.ScriptTask;
import org.eclipse.bpmn2.SendTask;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.ServiceTask;
import org.eclipse.bpmn2.SignalEventDefinition;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.TerminateEventDefinition;
import org.eclipse.bpmn2.TextAnnotation;
import org.eclipse.bpmn2.TimerEventDefinition;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.bpmn2.di.BpmnDiFactory;
import org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl;
import org.eclipse.bpmn2.util.Bpmn2XMLProcessor;

import de.uni_potsdam.hpi.bpt.ai.diagram.Diagram;
import de.uni_potsdam.hpi.bpt.ai.diagram.Shape;
import de.uni_potsdam.hpi.bpt.promnicat.persistenceApi.Representation;
import de.uni_potsdam.hpi.bpt.promnicat.util.IllegalTypeException;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.transformer.BpmaiJsonToDiagramUnit;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.IUnitData;
import de.uni_potsdam.hpi.bpt.promnicat.utilityUnits.unitData.UnitDataJbpt;

public class JsonToBpmn2Converter {

	// Factories to create BPMN2 and Diagram Elements
	private Bpmn2Factory fact = Bpmn2Factory.eINSTANCE;
	private BpmnDiFactory diFact = BpmnDiFactory.eINSTANCE;
	private DcFactory dcFact = DcFactory.eINSTANCE;

	// List of mapped BPMN elements
	private HashMap<String, BaseElement> bpmnElements = new HashMap<String, BaseElement>();
	// set to true if at least one collaboration element was found
	private boolean collaboration = false;
	//indicated unmapped elements
	private boolean incompleteDiagram = false;

	private String path = "";
	private Diagram diagram;
	private DocumentRoot documentRoot = fact.createDocumentRoot();

	private String[] shapes = { "Task", "StartNoneEvent", "StartSignalEvent", "StartMessageEvent", "StartTimerEvent", "StartRuleEvent", "StartEscalationEvent",
			"StartLinkEvent", "StartMultipleEvent", "StartCompensationEvent", "StartErrorEvent", "StartConditionalEvent", "EndNoneEvent", "EndEscalationEvent",
			"EndMessageEvent", "EndSignalEvent", "EndErrorEvent", "EndCancelEvent", "EndCancelEvent", "EndCompensationEvent", "EndLinkEvent",
			"EndMultipleEvent", "EndTerminateEvent", "IntermediateEvent", "IntermediateNoneEventCatching", "IntermediateMessageEventCatching",
			"IntermediateTimerEventCatching", "IntermediateErrorEventCatching", "IntermediateCancelEventCatching", "IntermediateCompensationEventCatching",
			"IntermediateRuleEventCatching", "IntermediateLinkEventCatching", "IntermediateMultipleEventCatching", "IntermediateSignalEventCatching",
			"IntermediateSignalEventThrowing", "IntermediateEscalationEventThrowing", "IntermediateMultipleEventThrowing", "IntermediateMessageEventThrowing",
			"IntermediateLinkEventThrowing", "IntermediateNoneEvent", "IntermediateMessageEvent", "IntermediateTimerEvent", "IntermediateErrorEvent",
			"IntermediateConditionalEvent", "IntermediateCancelEvent", "IntermediateCompensationEvent", "IntermediateRuleEvent", "IntermediateLinkEvent",
			"IntermediateMultipleEvent", "IntermediateCompensationEventThrowing", "Exclusive_Databased_Gateway", "Exclusive_Databased_Gateway",
			"EventbasedGateway", "InclusiveGateway", "ComplexGateway", "Lane", "Pool", "CollapsedPool", "DataObject", "Subprocess", "CollapsedSubprocess",
			"processparticipant", "TextAnnotation", "ITSystem", "Message", "DataStore", "Group", "IntermediateParallelMultipleEventCatching",
			"StartParallelMultipleEvent" };
	private String[] edges = { "SequenceFlow", "Association_Undirected", "Association_Unidirectional", "Association_Bidirectional", "MessageFlow" };

	private ArrayList<String> unmatchedItems = new ArrayList<String>();

	public HashMap<String, BaseElement> execute(String path) throws IllegalTypeException, IOException {
		this.path = path;
		String filepath = "converted" + path.substring(path.lastIndexOf("\\"), path.lastIndexOf(".")) + ".bpmn";
		diagram = jsonToDiagram(path);
		mapElement(diagram);
		insertCollaborationElements();
		HashMap<String, ArrayList<String>> sfTargets = updateFlows();
		updateLaneRefs();
		updateProcessRefs(sfTargets);
		createDiagramInformation();

		// Testblock für shapes
		//
		// for(Shape shape:diagram.getShapes()){
		// if(shape.getResourceId().equals("sid-854EA5B7-4319-4177-8603-98399B035155")){
		// System.out.println(shape);
		// System.out.println(shape.getIncomings());
		// System.out.println(shape.getTarget());
		// System.out.println(shape.getOutgoings());
		// }
		// }

		try {
			save(documentRoot, new FileOutputStream(filepath));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bpmnElements;
	}

	public Diagram jsonToDiagram(String path) throws IllegalTypeException {
		BpmaiJsonToDiagramUnit unit = new BpmaiJsonToDiagramUnit();
		Representation representation = new Representation();
		File file = new File(path);
		representation.importFile(file);
		IUnitData<Object> input = new UnitDataJbpt<Object>(representation);
		this.diagram = (Diagram) unit.execute(input).getValue();
		return diagram;
	}

	private void mapElement(Shape shape) throws IOException {
		switch (shape.getStencil().getId()) {
		case "BPMNDiagram": {
			addDefinitions(shape);
			break;
		}
		case "Pool": {
			addParticipant(shape);
			break;
		}
		case "CollapsedPool": {
			addParticipant(shape);
			break;
		}
		case "processparticipant": {
			addParticipant(shape);
			break;
		}
		case "Lane": {
			addLane(shape);
			break;
		}
		case "Task": {
			addTask(shape);
			break;
		}
		case "SequenceFlow": {
			addSequenceFlow(shape);
			break;
		}
		case "MessageFlow": {
			addMessageFlow(shape);
			break;
		}
		case "Association_Undirected": {
			addAssociation(shape, "undirected");
			break;
		}
		case "Association_Unidirectional": {
			addAssociation(shape, "unidirectional");
			break;
		}
		case "Association_Bidirectional": {
			addAssociation(shape, "bidirectional");
			break;
		}

		case "StartNoneEvent": {
			addStartEvent(shape, "None");
			break;
		}
		case "StartMessageEvent": {
			addStartEvent(shape, "Message");
			break;
		}
		case "StartEscalationEvent": {
			addStartEvent(shape, "Escalation");
			break;
		}
		case "StartTimerEvent": {
			addStartEvent(shape, "Timer");
			break;
		}
		case "StartRuleEvent": {
			addStartEvent(shape, "Rule");
			break;
		}
		case "StartSignalEvent": {
			addStartEvent(shape, "Signal");
			break;
		}
		case "StartLinkEvent": {
			addStartEvent(shape, "Link");
			break;
		}
		case "StartMultipleEvent": {
			addStartEvent(shape, "Multiple");
			break;
		}
		case "StartErrorEvent": {
			addStartEvent(shape, "Error");
			break;
		}
		case "StartConditionalEvent": {
			addStartEvent(shape, "Condiational");
			break;
		}
		case "StartCompensationEvent": {
			addStartEvent(shape, "Compensation");
			break;
		}
		case "EndNoneEvent": {
			addEndEvent(shape, "None");
			break;
		}
		case "EndEscalationEvent": {
			addEndEvent(shape, "Escalation");
			break;
		}
		case "EndMessageEvent": {
			addEndEvent(shape, "Message");
			break;
		}
		case "EndSignalEvent": {
			addEndEvent(shape, "Signal");
			break;
		}
		case "EndErrorEvent": {
			addEndEvent(shape, "Error");
			break;
		}
		case "EndCancelEvent": {
			addEndEvent(shape, "Cancel");
			break;
		}
		case "EndCompensationEvent": {
			addEndEvent(shape, "Compensation");
			break;
		}
		case "EndLinkEvent": {
			addEndEvent(shape, "Link");
			break;
		}
		case "EndMultipleEvent": {
			addEndEvent(shape, "Multiple");
			break;
		}
		case "EndTerminateEvent": {
			addEndEvent(shape, "Terminate");
			break;
		}
		case "IntermediateNoneEventCatching": {
			addIntermediateCatchEvent(shape, "None");
			break;
		}
		case "IntermediateMessageEventCatching": {
			addIntermediateCatchEvent(shape, "Message");
			break;
		}
		case "IntermediateTimerEventCatching": {
			addIntermediateCatchEvent(shape, "Timer");
			break;
		}
		case "IntermediateErrorEventCatching": {
			addIntermediateCatchEvent(shape, "Error");
			break;
		}
		case "IntermediateCancelEventCatching": {
			addIntermediateCatchEvent(shape, "Cancel");
			break;
		}
		case "IntermediateCompensationEventCatching": {
			addIntermediateCatchEvent(shape, "Compensation");
			break;
		}
		case "IntermediateRuleEventCatching": {
			addIntermediateCatchEvent(shape, "Rule");
			break;
		}
		case "IntermediateLinkEventCatching": {
			addIntermediateCatchEvent(shape, "Link");
			break;
		}
		case "IntermediateMultipleEventCatching": {
			addIntermediateCatchEvent(shape, "Multiple");
			break;
		}
		case "IntermediateSignalEventCatching": {
			addIntermediateCatchEvent(shape, "Signal");
			break;
		}
		case "IntermediateSignalEventThrowing": {
			addIntermediateCatchEvent(shape, "Signal");
			break;
		}
		case "IntermediateMessageEventThrowing": {
			addIntermediateThrowEvent(shape, "Message");
			break;
		}
		case "IntermediateLinkEventThrowing": {
			addIntermediateThrowEvent(shape, "Link");
			break;
		}
		case "IntermediateMultipleEventThrowing": {
			addIntermediateThrowEvent(shape, "Multiple");
			break;
		}
		case "IntermediateEscalationEventThrowing": {
			addIntermediateThrowEvent(shape, "Escalation");
			break;
		}
		case "IntermediateCompensationEventThrowing": {
			addIntermediateThrowEvent(shape, "Compensation");
			break;
		}
		case "IntermediateEvent": {
			addIntermediateCatchEvent(shape, "None");
			break;
		}
		case "IntermediateNoneEvent": {
			addIntermediateCatchEvent(shape, "None");
			break;
		}
		case "IntermediateConditionalEvent": {
			addIntermediateCatchEvent(shape, "Conditional");
			break;
		}
		case "IntermediateMessageEvent": {
			addIntermediateCatchEvent(shape, "Message");
			break;
		}
		case "IntermediateTimerEvent": {
			addIntermediateCatchEvent(shape, "Timer");
			break;
		}
		case "IntermediateErrorEvent": {
			addIntermediateCatchEvent(shape, "Error");
			break;
		}
		case "IntermediateCancelEvent": {
			addIntermediateCatchEvent(shape, "Cancel");
			break;
		}
		case "IntermediateCompensationEvent": {
			addIntermediateCatchEvent(shape, "Compensation");
			break;
		}
		case "IntermediateRuleEvent": {
			addIntermediateCatchEvent(shape, "Rule");
			break;
		}
		case "IntermediateLinkEvent": {
			addIntermediateCatchEvent(shape, "Link");
			break;
		}
		case "IntermediateEscalationEvent": {
			addIntermediateCatchEvent(shape, "Escalation");
			break;
		}
		case "IntermediateMultipleEvent": {
			addIntermediateCatchEvent(shape, "Multiple");
			break;
		}
		case "Exclusive_Databased_Gateway": {
			addExclusive_Databased_Gateway(shape);
			break;
		}
		case "ParallelGateway": {
			addParallelGateway(shape);
			break;
		}
		case "EventbasedGateway": {
			addEventBasedGateway(shape);
			break;
		}
		case "InclusiveGateway": {
			addInclusiveGateway(shape);
			break;
		}
		case "ComplexGateway": {
			addComplexGateway(shape);
			break;
		}
		case "DataObject": {
			addDataObject(shape);
			break;
		}
		case "Subprocess": {
			addSubprocess(shape, false);
			break;
		}
		case "CollapsedSubprocess": {
			addSubprocess(shape, false);
			break;
		}
		case "EventSubprocess": {
			addSubprocess(shape, true);
			break;
		}
		case "Group": {
			addGroup(shape);
			break;
		}
		case "TextAnnotation": {
			addTextAnnotation(shape);
			break;
		}
		case "ITSystem": {
			addITSystem(shape);
			break;
		}
		case "Message": {
			addMessage(shape);
			break;
		}
		case "DataStore": {
			addDataStore(shape);
			break;
		}
		case "StartParallelMultipleEvent": {
			System.out.println("Removed EventDefinitions from: " + shape.getResourceId());
			addStartEvent(shape, "None");
			break;
		}
		case "IntermediateParallelMultipleEventCatching": {
			System.out.println("Removed EventDefinitions from: " + shape.getResourceId());
			addIntermediateCatchEvent(shape, "None");
			break;
		}

		default: {
			// creates txt file containing all elementtypes which could not be
			// mapped
			if (!Files.readAllLines(Paths.get("UnmatchedItems.txt"), Charset.defaultCharset()).contains(shape.getStencilId().toString())) {
				Files.write(Paths.get("UnmatchedItems.txt"), ((shape.getStencilId() + System.getProperty("line.separator")).getBytes()),
						StandardOpenOption.APPEND);
				unmatchedItems.add(shape.getStencilId().toString());
			}
			// creates a txt file containing all paths to all diagrams with
			// unmapped elements
			if (!incompleteDiagram) {
				Files.write(Paths.get("IncompleteDiagrams.txt"), (path + System.getProperty("line.separator")).getBytes(), StandardOpenOption.APPEND);
				incompleteDiagram = true;
			}
			System.err.println("UnmatchedItem: " + shape.getStencilId());
		}

		}

		ArrayList<Shape> childs = shape.getChildShapes();
		for (Shape child : childs) {
			mapElement(child);
		}
	}

	private DataStore addDataStore(Shape shape) {
		DataStore data = fact.createDataStore();
		data.setId(shape.getResourceId());
		data.setName(shape.getProperty("name"));
		System.err.println("DataStore with unset attributes");
		data.setIsUnlimited(Boolean.parseBoolean(shape.getProperty("isUnlimited")));
		bpmnElements.put(shape.getResourceId(), data);
		return data;
	}

	private Message addMessage(Shape shape) {
		Message message = fact.createMessage();
		message.setId(shape.getResourceId());
		message.setName(shape.getProperty("Name"));
		bpmnElements.put(shape.getResourceId(), message);
		return message;
	}

	private TextAnnotation addITSystem(Shape shape) {
		System.out.println("No mapping for ITSystem, mapped to TextAnnotation");
		TextAnnotation text = fact.createTextAnnotation();
		text.setId(shape.getResourceId());
		text.setText(">>IT-System<<");
		bpmnElements.put(shape.getResourceId(), text);
		return text;
	}

	private TextAnnotation addTextAnnotation(Shape shape) {
		TextAnnotation text = fact.createTextAnnotation();
		text.setId(shape.getResourceId());
		text.setText(shape.getProperty("text"));
		text.setTextFormat(shape.getProperty("textformat"));
		bpmnElements.put(shape.getResourceId(), text);
		return text;
	}

	private ComplexGateway addComplexGateway(Shape shape) {
		ComplexGateway gateway = fact.createComplexGateway();
		gateway.setId(shape.getResourceId());
		gateway.setName(shape.getProperty("name"));

		if (!shape.getProperty("monitoring").isEmpty()) {
			Monitoring monitoring = fact.createMonitoring();
			monitoring.setId(shape.getProperty("monitoring"));
			gateway.setMonitoring(monitoring);
		}

		if (!shape.getProperty("auditing").isEmpty()) {
			Auditing auditing = fact.createAuditing();
			auditing.setId(shape.getProperty("auditing"));
			gateway.setAuditing(auditing);
		}

		bpmnElements.put(shape.getResourceId(), gateway);
		return gateway;

	}

	private InclusiveGateway addInclusiveGateway(Shape shape) {
		InclusiveGateway gateway = fact.createInclusiveGateway();
		gateway.setId(shape.getResourceId());
		gateway.setName(shape.getProperty("name"));

		if (!shape.getProperty("monitoring").isEmpty()) {
			Monitoring monitoring = fact.createMonitoring();
			monitoring.setId(shape.getProperty("monitoring"));
			gateway.setMonitoring(monitoring);
		}

		if (!shape.getProperty("auditing").isEmpty()) {
			Auditing auditing = fact.createAuditing();
			auditing.setId(shape.getProperty("auditing"));
			gateway.setAuditing(auditing);
		}

		bpmnElements.put(shape.getResourceId(), gateway);
		return gateway;

	}

	private Group addGroup(Shape shape) {
		Group group = fact.createGroup();
		group.setId(shape.getResourceId());
		bpmnElements.put(shape.getResourceId(), group);
		return group;
	}

	private Association addAssociation(Shape shape, String direction) {
		Association association = fact.createAssociation();
		association.setId(shape.getResourceId());
		switch (direction) {
		case "undirected":
			association.setAssociationDirection(AssociationDirection.NONE);
		case "unidirectional":
			association.setAssociationDirection(AssociationDirection.ONE);
		case "bidirectional":
			association.setAssociationDirection(AssociationDirection.BOTH);
		}

		bpmnElements.put(shape.getResourceId(), association);
		return association;
	}

	private EventBasedGateway addEventBasedGateway(Shape shape) {
		EventBasedGateway gateway = fact.createEventBasedGateway();
		gateway.setId(shape.getResourceId());
		gateway.setName(shape.getProperty("name"));

		try {
			if (shape.getProperty("gatewaytype").equals("XOR"))
				gateway.setEventGatewayType(EventBasedGatewayType.EXCLUSIVE);
			else
				gateway.setEventGatewayType(EventBasedGatewayType.PARALLEL);
		} catch (NullPointerException e) {

		}
		// TODO can throw NullPointer
		if (!shape.getProperty("monitoring").isEmpty()) {
			Monitoring monitoring = fact.createMonitoring();
			monitoring.setId(shape.getProperty("monitoring"));
			gateway.setMonitoring(monitoring);
		}

		if (!shape.getProperty("auditing").isEmpty()) {
			Auditing auditing = fact.createAuditing();
			auditing.setId(shape.getProperty("auditing"));
			gateway.setAuditing(auditing);
		}

		bpmnElements.put(shape.getResourceId(), gateway);
		return gateway;

	}

	private SubProcess addSubprocess(Shape shape, boolean triggeredByEvent) {
		SubProcess subProcess = fact.createSubProcess();

		subProcess.setId(shape.getResourceId());
		subProcess.setName(shape.getProperty("name"));
		subProcess.setCompletionQuantity(Integer.parseInt(shape.getProperty("completionquantity")));
		subProcess.setIsForCompensation(Boolean.parseBoolean(shape.getProperty("isforcompensation")));

		if (shape.getProperty("looptype").equals("Standart")) {
			LoopCharacteristics loopCharacteristics = fact.createStandardLoopCharacteristics();
			subProcess.setLoopCharacteristics(loopCharacteristics);
		}

		if (shape.getProperty("looptype").equals("MultiInstance")) {
			LoopCharacteristics loopCharacteristics = fact.createMultiInstanceLoopCharacteristics();
			subProcess.setLoopCharacteristics(loopCharacteristics);
		}

		if (!shape.getProperty("monitoring").isEmpty()) {
			Monitoring monitoring = fact.createMonitoring();
			monitoring.setId(shape.getProperty("monitoring"));
			subProcess.setMonitoring(monitoring);
		}

		subProcess.setStartQuantity(Integer.parseInt(shape.getProperty("startquantity")));

		if (!shape.getProperty("auditing").isEmpty()) {
			Auditing auditing = fact.createAuditing();
			auditing.setId(shape.getProperty("auditing"));
			subProcess.setAuditing(auditing);
		}

		subProcess.setTriggeredByEvent(triggeredByEvent);
		bpmnElements.put(shape.getResourceId(), subProcess);
		return subProcess;

	}

	private DataObject addDataObject(Shape shape) {
		DataObject dataObject = fact.createDataObject();
		dataObject.setId(shape.getResourceId());
		dataObject.setName(shape.getProperty("name"));

		if (!shape.getProperty("iscollection").isEmpty())
			dataObject.setIsCollection(Boolean.parseBoolean(shape.getProperty("iscollection")));

		if (!shape.getProperty("monitoring").isEmpty()) {
			Monitoring monitoring = fact.createMonitoring();
			monitoring.setId(shape.getProperty("monitoring"));
			dataObject.setMonitoring(monitoring);
		}

		if (!shape.getProperty("auditing").isEmpty()) {
			Auditing auditing = fact.createAuditing();
			auditing.setId(shape.getProperty("auditing"));
			dataObject.setAuditing(auditing);
		}

		bpmnElements.put(shape.getResourceId(), dataObject);
		return dataObject;
	}

	private ParallelGateway addParallelGateway(Shape shape) {
		ParallelGateway gateway = fact.createParallelGateway();
		gateway.setId(shape.getResourceId());
		gateway.setName(shape.getProperty("name"));

		if (!shape.getProperty("monitoring").isEmpty()) {
			Monitoring monitoring = fact.createMonitoring();
			monitoring.setId(shape.getProperty("monitoring"));
			gateway.setMonitoring(monitoring);
		}

		if (!shape.getProperty("auditing").isEmpty()) {
			Auditing auditing = fact.createAuditing();
			auditing.setId(shape.getProperty("auditing"));
			gateway.setAuditing(auditing);
		}

		bpmnElements.put(shape.getResourceId(), gateway);
		return gateway;
	}

	private IntermediateThrowEvent addIntermediateThrowEvent(Shape shape, String eventType) {
		IntermediateThrowEvent interEvent = fact.createIntermediateThrowEvent();
		interEvent.setId(shape.getResourceId());
		interEvent.setName(shape.getProperty("name"));

		if (!shape.getProperty("auditing").isEmpty()) {
			Auditing auditing = fact.createAuditing();
			auditing.setId(shape.getProperty("auditing"));
			interEvent.setAuditing(auditing);
		}

		if (!shape.getProperty("monitoring").isEmpty()) {
			Monitoring monitoring = fact.createMonitoring();
			monitoring.setId(shape.getProperty("monitoring"));
			interEvent.setMonitoring(monitoring);
		}

		if (!(createEventDefinition(shape, eventType) == null))
			interEvent.getEventDefinitions().add(createEventDefinition(shape, eventType));

		bpmnElements.put(shape.getResourceId(), interEvent);
		return interEvent;
	}

	private EventDefinition createEventDefinition(Shape shape, String eventType) {
		EventDefinition eventDefinition = null;
		switch (eventType) {
		case "Message": {
			MessageEventDefinition message = fact.createMessageEventDefinition();
			// TODO throws NullPointer
			// if (!shape.getProperty("messageref").isEmpty()) {
			// message.setMessageRef((Message) bpmnElements.get(shape
			// .getProperty("messageref")));
			// }
			eventDefinition = message;
			break;
		}
		case "Timer": {
			TimerEventDefinition timer = fact.createTimerEventDefinition();
			FormalExpression expression = fact.createFormalExpression();
			if (!shape.getProperty("timecycle").isEmpty()) {
				expression.setBody(shape.getProperty("timecycle"));
				timer.setTimeCycle(expression);
			}
			if (!shape.getProperty("timedate").isEmpty()) {
				expression.setBody(shape.getProperty("timedate"));
				timer.setTimeDate(expression);
			}
			if (!shape.getProperty("timeduration").isEmpty()) {
				expression.setBody(shape.getProperty("timeduration"));
				timer.setTimeDuration(expression);
			}
			eventDefinition = timer;
			break;
		}
		case "Error": {
			ErrorEventDefinition error = fact.createErrorEventDefinition();
			eventDefinition = error;
			break;
		}
		case "Cancel": {
			CancelEventDefinition cancel = fact.createCancelEventDefinition();
			eventDefinition = cancel;
			break;
		}
		case "Compensation": {
			CompensateEventDefinition compensation = fact.createCompensateEventDefinition();
			eventDefinition = compensation;
		}
		case "Rule": {
			System.out.println("No mapping for RuleEvent, mapped to Event");
			break;
		}
		case "Link": {
			LinkEventDefinition link = fact.createLinkEventDefinition();
			eventDefinition = link;
			break;
		}
		case "Multiple": {
			System.out.println("No mapping in  for MultipleEvent, mapped to Event");
			break;
		}
		case "Terminate": {
			TerminateEventDefinition terminate = fact.createTerminateEventDefinition();
			eventDefinition = terminate;
			break;
		}
		case "Signal": {
			SignalEventDefinition signal = fact.createSignalEventDefinition();
			eventDefinition = signal;
		}
		case "Escalation": {
			EscalationEventDefinition escalation = fact.createEscalationEventDefinition();
			eventDefinition = escalation;
		}
		case "Condiational": {
			ConditionalEventDefinition condition = fact.createConditionalEventDefinition();
			//FormalExpression expression = fact.createFormalExpression();
			// TODO throws NullPointer
			// if (!shape.getProperty("condition").isEmpty()) {
			// expression.setBody(shape.getProperty("condition"));
			// condition.setCondition(expression);
			// }
			eventDefinition = condition;
		}
		}
		return eventDefinition;
	}

	private IntermediateCatchEvent addIntermediateCatchEvent(Shape shape, String eventType) {
		IntermediateCatchEvent interEvent = fact.createIntermediateCatchEvent();
		interEvent.setId(shape.getResourceId());
		interEvent.setName(shape.getProperty("name"));

		if (!shape.getProperty("auditing").isEmpty()) {
			Auditing auditing = fact.createAuditing();
			auditing.setId(shape.getProperty("auditing"));
			interEvent.setAuditing(auditing);
		}

		if (!shape.getProperty("monitoring").isEmpty()) {
			Monitoring monitoring = fact.createMonitoring();
			monitoring.setId(shape.getProperty("monitoring"));
			interEvent.setMonitoring(monitoring);
		}

		if (!(createEventDefinition(shape, eventType) == null))
			interEvent.getEventDefinitions().add(createEventDefinition(shape, eventType));

		bpmnElements.put(shape.getResourceId(), interEvent);
		return interEvent;

	}

	private void createDiagramInformation() {
		for (Shape shape : diagram.getShapes()) {
			if (Arrays.asList(edges).contains(shape.getStencil().getId()))
				setEdgeDiagramInformation(shape);

			if (Arrays.asList(shapes).contains(shape.getStencil().getId()))
				setShapeDiagramInformation(shape);
		}
	}

	private void setEdgeDiagramInformation(Shape shape) {
		BPMNEdge edge = diFact.createBPMNEdge();
		Point spoint = dcFact.createPoint();

		for (Shape incoming : shape.getIncomings()) {
			spoint.setX(incoming.getUpperLeft().getX().floatValue());
			spoint.setY(incoming.getUpperLeft().getY().floatValue());
			edge.getWaypoint().add(spoint);
		}
		Point tpoint = dcFact.createPoint();

		for (Shape outgoing : shape.getOutgoings()) {
			tpoint.setX(outgoing.getUpperLeft().getX().floatValue());
			tpoint.setY(outgoing.getUpperLeft().getY().floatValue());
			edge.getWaypoint().add(tpoint);
		}

		edge.setBpmnElement(bpmnElements.get(shape.getResourceId()));
		documentRoot.getDefinitions().getDiagrams().get(0).getPlane().getPlaneElement().add(edge);
	}

	private void setShapeDiagramInformation(Shape shape) {
		BPMNShape bpmnShape = diFact.createBPMNShape();
		Bounds bounds = dcFact.createBounds();
		bounds.setHeight((float) shape.getHeight());
		bounds.setWidth((float) shape.getWidth());
		bounds.setX(shape.getUpperLeft().getX().floatValue());
		bounds.setY(shape.getUpperLeft().getY().floatValue());
		bpmnShape.setBounds(bounds);

		if (shape.getStencilId().equals("CollapsedSubprocess"))
			bpmnShape.setIsExpanded(false);

		if (shape.getStencilId().equals("Subprocess"))
			bpmnShape.setIsExpanded(true);

		bpmnShape.setBpmnElement(bpmnElements.get(shape.getResourceId()));
		documentRoot.getDefinitions().getDiagrams().get(0).getPlane().getPlaneElement().add(bpmnShape);

	}

	private MessageFlow addMessageFlow(Shape shape) {
		MessageFlow flow = fact.createMessageFlow();
		flow.setId(shape.getResourceId());
		flow.setName(shape.getProperty("name"));

		// TODO throws NullPointer
		// if (!shape.getProperty("messageref").isEmpty()) {
		// flow.setMessageRef((Message) bpmnElements.get(shape
		// .getProperty("messageref")));
		// }

		bpmnElements.put(shape.getResourceId(), flow);
		collaboration = true;
		return flow;

	}

	private ExclusiveGateway addExclusive_Databased_Gateway(Shape shape) {
		ExclusiveGateway gateway = fact.createExclusiveGateway();
		gateway.setId(shape.getResourceId());
		gateway.setName(shape.getProperty("name"));

		if (!shape.getProperty("monitoring").isEmpty()) {
			Monitoring monitoring = fact.createMonitoring();
			monitoring.setId(shape.getProperty("monitoring"));
			gateway.setMonitoring(monitoring);
		}

		if (!shape.getProperty("auditing").isEmpty()) {
			Auditing auditing = fact.createAuditing();
			auditing.setId(shape.getProperty("auditing"));
			gateway.setAuditing(auditing);
		}

		bpmnElements.put(shape.getResourceId(), gateway);
		return gateway;
	}

	private Definitions addDefinitions(Shape shape) {
		Definitions def = fact.createDefinitions();
		def.setId(shape.getResourceId());
		def.setTypeLanguage(shape.getProperty("typelanguage"));
		def.setExpressionLanguage(shape.getProperty("expressionlanguage"));
		def.setName(shape.getProperty("name"));
		def.setTargetNamespace(shape.getProperty("targetnamespace"));

		// Create the document root
		documentRoot.setDefinitions(def);

		// Create the diagram
		BPMNDiagram diagram = diFact.createBPMNDiagram();
		diagram.setPlane(diFact.createBPMNPlane());
		diagram.setName(shape.getResourceId());
		documentRoot.getDefinitions().getDiagrams().add(diagram);

		bpmnElements.put(shape.getResourceId(), def);

		return def;
	}

	private Participant addParticipant(Shape shape) {
		Participant part = fact.createParticipant();
		part.setId(shape.getResourceId());
		part.setName(shape.getProperty("name"));

		ParticipantMultiplicity multiplicity = fact.createParticipantMultiplicity();
		try {
			multiplicity.setMinimum(Integer.parseInt(shape.getProperty("minimum")));
		} catch (NumberFormatException e) {
			System.err.println(shape.getResourceId() + " Invalid minimum");
		}
		try {
			multiplicity.setMaximum(Integer.parseInt(shape.getProperty("maximum")));
		} catch (NumberFormatException e) {
			System.err.println(shape.getResourceId() + " Invalid minimum");
		}

		part.setParticipantMultiplicity(multiplicity);

		bpmnElements.put(shape.getResourceId(), part);
		collaboration = true;
		return part;
	}

	private Lane addLane(Shape shape) {
		Lane lane = fact.createLane();
		lane.setId(shape.getResourceId());
		lane.setName(shape.getProperty("name"));
		bpmnElements.put(shape.getResourceId(), lane);
		return lane;
	}

	private Task createTypedTask(Shape shape) {
		switch (shape.getProperty("tasktype")) {
		case "Service":
			return fact.createServiceTask();
		case "Recieve":
			return fact.createReceiveTask();
		case "Send":
			return fact.createSendTask();
		case "User":
			return fact.createUserTask();
		case "Script":
			return fact.createScriptTask();
		case "Manual":
			return fact.createManualTask();
		case "Reference": {
			System.err.println("ReferenceEvents have been removed from standard, mapped to Task");
			return fact.createTask();
		}
		default: {
			return fact.createTask();
		}
		}

	}

	private Task addTask(Shape shape) {
		Task task = createTypedTask(shape);
		task.setId(shape.getResourceId());
		task.setName(shape.getProperty("name"));
		task.setCompletionQuantity(Integer.parseInt(shape.getProperty("completionquantity")));
		task.setIsForCompensation(Boolean.parseBoolean(shape.getProperty("isforcompensation")));

		if (shape.getProperty("looptype").equals("Standart")) {
			LoopCharacteristics loopCharacteristics = fact.createStandardLoopCharacteristics();
			task.setLoopCharacteristics(loopCharacteristics);
		}

		if (shape.getProperty("looptype").equals("MultiInstance")) {
			LoopCharacteristics loopCharacteristics = fact.createMultiInstanceLoopCharacteristics();
			task.setLoopCharacteristics(loopCharacteristics);
		}

		if (!shape.getProperty("monitoring").isEmpty()) {
			Monitoring monitoring = fact.createMonitoring();
			monitoring.setId(shape.getProperty("monitoring"));
			task.setMonitoring(monitoring);
		}

		task.setStartQuantity(Integer.parseInt(shape.getProperty("startquantity")));

		if (!shape.getProperty("auditing").isEmpty()) {
			Auditing auditing = fact.createAuditing();
			auditing.setId(shape.getProperty("auditing"));
			task.setAuditing(auditing);
		}

		bpmnElements.put(shape.getResourceId(), task);

		return task;
	}

	private SequenceFlow addSequenceFlow(Shape shape) {
		SequenceFlow flow = fact.createSequenceFlow();
		flow.setId(shape.getResourceId());
		flow.setName(shape.getProperty("name"));

		if (!shape.getProperty("auditing").isEmpty()) {
			Auditing auditing = fact.createAuditing();
			auditing.setId(shape.getProperty("auditing"));
			flow.setAuditing(auditing);
		}

		if (shape.getProperty("ConditionType") == "Expression") {
			Expression expression = fact.createExpression();
			expression.setId(shape.getProperty("ConditionExpression"));
			flow.setConditionExpression(expression);
		}

		flow.setIsImmediate(Boolean.parseBoolean(shape.getProperty("isimmediate")));

		if (!shape.getProperty("monitoring").isEmpty()) {
			Monitoring monitoring = fact.createMonitoring();
			monitoring.setId(shape.getProperty("monitoring"));
			flow.setMonitoring(monitoring);
		}

		bpmnElements.put(shape.getResourceId(), flow);
		return flow;
	}

	private EndEvent addEndEvent(Shape shape, String eventType) {
		EndEvent endEvent = fact.createEndEvent();
		endEvent.setId(shape.getResourceId());
		endEvent.setName(shape.getProperty("name"));

		if (!shape.getProperty("auditing").isEmpty()) {
			Auditing auditing = fact.createAuditing();
			auditing.setId(shape.getProperty("auditing"));
			endEvent.setAuditing(auditing);
		}

		if (!shape.getProperty("monitoring").isEmpty()) {
			Monitoring monitoring = fact.createMonitoring();
			monitoring.setId(shape.getProperty("monitoring"));
			endEvent.setMonitoring(monitoring);
		}

		EventDefinition eventDefinition = createEventDefinition(shape, eventType);

		if (!(eventDefinition == null))
			endEvent.getEventDefinitions().add(createEventDefinition(shape, eventType));

		bpmnElements.put(shape.getResourceId(), endEvent);
		return endEvent;
	}

	private StartEvent addStartEvent(Shape shape, String eventType) {
		StartEvent startEvent = fact.createStartEvent();
		startEvent.setId(shape.getResourceId());
		startEvent.setName(shape.getProperty("name"));

		if (!shape.getProperty("auditing").isEmpty()) {
			Auditing auditing = fact.createAuditing();
			auditing.setId(shape.getProperty("auditing"));
			startEvent.setAuditing(auditing);
		}

		if (!shape.getProperty("monitoring").isEmpty()) {
			Monitoring monitoring = fact.createMonitoring();
			monitoring.setId(shape.getProperty("monitoring"));
			startEvent.setMonitoring(monitoring);
		}

		EventDefinition eventDefinition = createEventDefinition(shape, eventType);

		if (!(eventDefinition == null))
			startEvent.getEventDefinitions().add(createEventDefinition(shape, eventType));

		bpmnElements.put(shape.getResourceId(), startEvent);
		return startEvent;
	}

	private HashMap<String, ArrayList<String>> updateFlows() {
		HashMap<String, ArrayList<String>> sfTargets = new HashMap<String, ArrayList<String>>();
		for (Shape shape : this.diagram.getShapes()) {

			/*
			 * Create target and source references for SequenceFlows
			 */
			if ((bpmnElements.get(shape.getResourceId()) instanceof SequenceFlow)) {
				for (Shape incoming : shape.getIncomings())
					try {
						((SequenceFlow) bpmnElements.get(shape.getResourceId())).setSourceRef((FlowNode) bpmnElements.get(incoming.getResourceId()));
					} catch (ClassCastException e) {
						System.err.println("SequenceFlow " + shape.getResourceId() + " can not set incoming " + incoming.getResourceId());
					}
				for (Shape outgoing : shape.getOutgoings())
					try {
						((SequenceFlow) bpmnElements.get(shape.getResourceId())).setTargetRef((FlowNode) bpmnElements.get(outgoing.getResourceId()));
					} catch (ClassCastException e) {
						System.err.println("SequenceFlow " + shape.getResourceId() + " can not set outgoing " + outgoing.getResourceId());
					}
				// add SequenceFlow to SF-TargetMap
				try {
					if (sfTargets.containsKey(shape.getTarget())) {
						sfTargets.get(shape.getTarget()).add(shape.getResourceId());
					} else {
						ArrayList<String> list = new ArrayList<String>();
						list.add(shape.getResourceId());
						sfTargets.put(shape.getTarget().getResourceId(), list);
					}
				} catch (NullPointerException e) {
					System.err.println("SquenceFlow" + shape.getResourceId() + "cannot be added to right Process since it has no target");
				}
			} else if ((bpmnElements.get(shape.getResourceId()) instanceof Association)) {
				for (Shape incoming : shape.getIncomings())
					((Association) bpmnElements.get(shape.getResourceId())).setSourceRef(bpmnElements.get(incoming.getResourceId()));
				for (Shape outgoing : shape.getOutgoings())
					((Association) bpmnElements.get(shape.getResourceId())).setTargetRef(bpmnElements.get(outgoing.getResourceId()));
			} else if ((bpmnElements.get(shape.getResourceId()) instanceof MessageFlow)) {
				for (Shape incoming : shape.getIncomings()) {
					if (bpmnElements.get(incoming.getResourceId()) instanceof SubProcess || bpmnElements.get(incoming.getResourceId()) instanceof Association)
						System.err.println("MessageFlow: " + shape.getResourceId() + "incoming can not be set");
					else
						((MessageFlow) bpmnElements.get(shape.getResourceId())).setSourceRef((InteractionNode) bpmnElements.get(incoming.getResourceId()));
				}
				for (Shape outgoing : shape.getOutgoings()) {
					if (bpmnElements.get(outgoing.getResourceId()) instanceof SubProcess || bpmnElements.get(outgoing.getResourceId()) instanceof Association)
						System.err.println("MessageFlow: " + shape.getResourceId() + " outgoing can not be set");
					else
						((MessageFlow) bpmnElements.get(shape.getResourceId())).setTargetRef((InteractionNode) bpmnElements.get(outgoing.getResourceId()));
				}
			} else if ((bpmnElements.get(shape.getResourceId()) instanceof Gateway)) {
				for (Shape incoming : shape.getIncomings())
					if (!(bpmnElements.get(incoming.getResourceId()) instanceof Association))
						((Gateway) bpmnElements.get(shape.getResourceId())).getIncoming().add((SequenceFlow) bpmnElements.get(incoming.getResourceId()));
				for (Shape outgoing : shape.getOutgoings())
					if (!(bpmnElements.get(outgoing.getResourceId()) instanceof Association))
						((Gateway) bpmnElements.get(shape.getResourceId())).getOutgoing().add((SequenceFlow) bpmnElements.get(outgoing.getResourceId()));
			}
		}
		return sfTargets;
	}

	private void updateEventMessageRef() {
		for (Shape shape : this.diagram.getShapes()) {
			if ((bpmnElements.get(shape.getResourceId()) instanceof StartEvent)) {
				for (EventDefinition eventdef : ((StartEvent) bpmnElements.get(shape.getResourceId())).getEventDefinitions()) {
					if (eventdef instanceof MessageEventDefinition) {
						((MessageEventDefinition) eventdef).setMessageRef((Message) bpmnElements.get(shape.getOutgoings().get(0).getResourceId()));
					}
				}
			}
		}
	}

	private void insertCollaborationElements() {
		if (!collaboration) {
			// Create a process for process diagram
			org.eclipse.bpmn2.Process process = fact.createProcess();
			process.setId("_process");
			LaneSet laneset = fact.createLaneSet();
			process.getLaneSets().add(laneset);
			documentRoot.getDefinitions().getRootElements().add(process);
			documentRoot.getDefinitions().getDiagrams().get(0).getPlane().setBpmnElement(process);
			for (BaseElement element : bpmnElements.values()) {
				if (element instanceof Lane)
					process.getLaneSets().get(0).getLanes().add((Lane) element);

				if (element instanceof FlowElement)
					process.getFlowElements().add((FlowElement) element);

				if (element instanceof Artifact)
					process.getArtifacts().add((Artifact) element);

				if (element instanceof RootElement)
					documentRoot.getDefinitions().getRootElements().add((RootElement) element);

			}
		} else {
			Collaboration collaboration = fact.createCollaboration();
			documentRoot.getDefinitions().getRootElements().add(collaboration);
			documentRoot.getDefinitions().getDiagrams().get(0).getPlane().setBpmnElement(collaboration);
			System.out.println("collabortionelements found");
			for (BaseElement element : bpmnElements.values()) {
				if (element instanceof Participant) {
					org.eclipse.bpmn2.Process process = fact.createProcess();
					process.setId(element.getId() + "_process");
					LaneSet laneset = fact.createLaneSet();
					process.getLaneSets().add(laneset);

					documentRoot.getDefinitions().getRootElements().add(process);
					((Participant) element).setProcessRef(process);
					collaboration.getParticipants().add((Participant) element);
				}
				if (element instanceof MessageFlow) {
					collaboration.getMessageFlows().add((MessageFlow) element);
				}
				if (element instanceof RootElement) {
					documentRoot.getDefinitions().getRootElements().add((RootElement) element);
				}
			}
		}
	}

	private void updateLaneRefs() {
		for (Shape shape : diagram.getShapes()) {

			if (bpmnElements.get(shape.getResourceId()) instanceof Lane) {
				boolean firstChildlane = true;
				for (Shape childshape : shape.getChildShapes()) {
					if (bpmnElements.get(childshape.getResourceId()) instanceof FlowNode) {
						((Lane) bpmnElements.get(shape.getResourceId())).getFlowNodeRefs().add(((FlowNode) bpmnElements.get(childshape.getResourceId())));
					}
					if (bpmnElements.get(childshape.getResourceId()) instanceof Lane) {
						if (firstChildlane) {
							LaneSet laneset = fact.createLaneSet();
							((Lane) bpmnElements.get(shape.getResourceId())).setChildLaneSet(laneset);
							firstChildlane = false;
						}
						((Lane) bpmnElements.get(shape.getResourceId())).getChildLaneSet().getLanes().add((Lane) bpmnElements.get(childshape.getResourceId()));
					}

				}

			}
		}
	}

	// Adds all FlowElements of lane and its ChildLanes to Participant
	// identified by Id
	// added FlowElements are added to existing addedFlowElements List and
	// joined list is returned
	private ArrayList<String> addLaneElementsToSubprocess(Lane lane, String subprocess, ArrayList<String> addedFlowElements,
			HashMap<String, ArrayList<String>> sfTargets) {
		for (FlowElement flowElement : lane.getFlowNodeRefs()) {
			((SubProcess) bpmnElements.get(subprocess)).getFlowElements().add(flowElement);
			addedFlowElements.add(flowElement.getId());
			// add all SequenceFlow with Target in Process to Process
			if (sfTargets.containsKey(flowElement.getId())) {
				for (String sequenceFlow : sfTargets.get(flowElement.getId())) {
					((SubProcess) bpmnElements.get(subprocess)).getFlowElements().add((FlowElement) bpmnElements.get(sequenceFlow));
					addedFlowElements.add(sequenceFlow);
				}
			}
		}
		try {
			for (Lane childlane : lane.getChildLaneSet().getLanes()) {
				addLaneElementsToSubprocess(childlane, subprocess, addedFlowElements, sfTargets);
			}
		} catch (NullPointerException e) {

		}
		return addedFlowElements;
	}

	// Adds all FlowElements of lane and its ChildLanes to Subprocess identified
	// by Id
	//
	private ArrayList<String> addLaneElementsToParticipant(Lane lane, String participant, ArrayList<String> addedFlowElements,
			HashMap<String, ArrayList<String>> sfTargets) {
		for (FlowElement flowElement : lane.getFlowNodeRefs()) {
			((Participant) bpmnElements.get(participant)).getProcessRef().getFlowElements().add(flowElement);
			addedFlowElements.add(flowElement.getId());
			// add all SequenceFlow with Target in Process to Process
			if (sfTargets.containsKey(flowElement.getId())) {
				for (String sequenceFlow : sfTargets.get(flowElement.getId())) {
					((Participant) bpmnElements.get(participant)).getProcessRef().getFlowElements().add((FlowElement) bpmnElements.get(sequenceFlow));
					addedFlowElements.add(sequenceFlow);
				}
			}

		}
		try {
			for (Lane childlane : lane.getChildLaneSet().getLanes()) {
				addLaneElementsToParticipant(childlane, participant, addedFlowElements, sfTargets);
			}
		} catch (NullPointerException e) {

		}
		return addedFlowElements;
	}

	private void updateProcessRefs(HashMap<String, ArrayList<String>> sfTargets) {
		ArrayList<String> addedFlowElements = new ArrayList<String>();
		String firstPool = "";
		for (Shape shape : diagram.getShapes()) {

			// create praticipants references
			if (bpmnElements.get(shape.getResourceId()) instanceof Participant) {
				firstPool = shape.getResourceId();
				for (Shape childshape : shape.getChildShapes()) {
					// add all Lanes belonging to the process of this paricipant
					if (bpmnElements.get(childshape.getResourceId()) instanceof Lane) {
						Lane lane = (Lane) bpmnElements.get(childshape.getResourceId());
						((Participant) bpmnElements.get(shape.getResourceId())).getProcessRef().getLaneSets().get(0).getLanes().add(lane);
						addedFlowElements = addLaneElementsToParticipant(lane, shape.getResourceId(), addedFlowElements, sfTargets);
					}

					// add all FlowElements belonging to the process of this
					// participant
					try {
						((Participant) bpmnElements.get(shape.getResourceId())).getProcessRef().getFlowElements()
								.add((FlowElement) bpmnElements.get(childshape.getResourceId()));
						addedFlowElements.add(childshape.getResourceId());

						// add all SequenceFlow with Target in Process to
						// Process
						if (sfTargets.containsKey(childshape.getResourceId())) {
							for (String sequenceFlow : sfTargets.get(childshape.getResourceId())) {
								((Participant) bpmnElements.get(shape.getResourceId())).getProcessRef().getFlowElements()
										.add((FlowElement) bpmnElements.get(sequenceFlow));
								addedFlowElements.add(sequenceFlow);
							}
						}
					} catch (Exception e) {

					}

					// add all Artifacts belonging to the process of this
					// participant
					try {
						((Participant) bpmnElements.get(shape.getResourceId())).getProcessRef().getArtifacts()
								.add((Artifact) bpmnElements.get(childshape.getResourceId()));
						addedFlowElements.add(childshape.getResourceId());
					} catch (Exception e) {

					}

				}
			}

			// create references of subprocesses
			if (bpmnElements.get(shape.getResourceId()) instanceof SubProcess) {
				boolean firstLane = true;
				for (Shape childshape : shape.getChildShapes()) {
					// add all Lanes belonging to this process
					if (bpmnElements.get(childshape.getResourceId()) instanceof Lane) {
						if (firstLane) {
							LaneSet laneset = fact.createLaneSet();
							((SubProcess) bpmnElements.get(shape.getResourceId())).getLaneSets().add(laneset);
							firstLane = false;
						}
						Lane lane = (Lane) bpmnElements.get(childshape.getResourceId());
						((SubProcess) bpmnElements.get(shape.getResourceId())).getLaneSets().get(0).getLanes().add(lane);
						addedFlowElements = addLaneElementsToSubprocess(lane, shape.getResourceId(), addedFlowElements, sfTargets);
					}
					// add all FlowElements belonging to this process
					try {
						((SubProcess) bpmnElements.get(shape.getResourceId())).getFlowElements()
								.add((FlowElement) bpmnElements.get(childshape.getResourceId()));
						addedFlowElements.add(childshape.getResourceId());
						// add all SequenceFlow with Target in Process to
						// Process
						if (sfTargets.containsKey(childshape.getResourceId())) {
							for (String sequenceFlow : sfTargets.get(childshape.getResourceId())) {
								((SubProcess) bpmnElements.get(shape.getResourceId())).getFlowElements().add((FlowElement) bpmnElements.get(sequenceFlow));
								addedFlowElements.add(sequenceFlow);
							}
						}
					} catch (Exception e) {

					}
					// add all Artifacts belonging to this process
					try {
						((SubProcess) bpmnElements.get(shape.getResourceId())).getArtifacts().add((Artifact) bpmnElements.get(childshape.getResourceId()));
						addedFlowElements.add(childshape.getResourceId());
					} catch (Exception e) {

					}
				}
			}
		}
		for (Shape shape : diagram.getShapes()) {
			if (!addedFlowElements.contains(shape.getResourceId())) {
				// add flow elements which belong to no lane to process1, if no
				// collaboration add to process
				try {
					if (!firstPool.isEmpty())
						((Participant) bpmnElements.get(firstPool)).getProcessRef().getFlowElements()
								.add((FlowElement) bpmnElements.get(shape.getResourceId()));
					else
						documentRoot.getProcess().getFlowElements().add((FlowElement) bpmnElements.get(shape.getResourceId()));

				} catch (Exception e) {
					continue;
				}
			}
		}
		for (Shape shape : diagram.getShapes()) {
			if (!addedFlowElements.contains(shape.getResourceId())) {
				// add artifacts which belong to no lane to process1, if no
				// collaboration add to process
				try {
					if (!firstPool.isEmpty())
						((Participant) bpmnElements.get(firstPool)).getProcessRef().getArtifacts().add((Artifact) bpmnElements.get(shape.getResourceId()));
					else
						documentRoot.getProcess().getArtifacts().add((Artifact) bpmnElements.get(shape.getResourceId()));

				} catch (Exception e) {
					continue;
				}
			}
		}
	}

	public void save(DocumentRoot model, OutputStream out) throws IOException {
		Bpmn2ResourceFactoryImpl resourceFactory = new Bpmn2ResourceFactoryImpl();
		File tempFile = File.createTempFile("bpmn20convert", "tmp");
		try {
			Resource resource = resourceFactory.createResource(URI.createFileURI(tempFile.getAbsolutePath()));
			resource.getContents().add(model);
			Bpmn2XMLProcessor proc = new Bpmn2XMLProcessor();
			Map<Object, Object> options = new HashMap<Object, Object>();

			proc.save(out, resource, options);
		} finally {
			tempFile.delete();
		}
	}
}
