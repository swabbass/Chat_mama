package com.example.mama;

import java.util.ArrayList;
import java.util.List;

public class Conversation {

	private Contact sender, receiver;
	private int lastMsg;
	private List<Message> msgs;
	private Long id;

	public Conversation(Contact sender, Contact receiver) {
		setSender(sender);
		setReceiver(receiver);
		this.lastMsg = 0;
		msgs = new ArrayList<Message>();
		// TODO Auto-generated constructor stub
	}

	public Conversation(String from, String from2) {
		// TODO Auto-generated constructor stub
		sender = new Contact("", from, "number", "mail");
		receiver = new Contact("", from2, "number", "mail");
		this.lastMsg = 0;
		msgs = new ArrayList<Message>();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Conversation) {
			return ((Conversation) o).getSender().equals(this.sender);
		}
		return false;
	}

	public void addMsg(Message m) {
		if (m != null) {
			msgs.add(lastMsg, m);
			this.lastMsg++;
		} else
			throw new RuntimeException("Conversations is null");
	}

	public boolean removeMsg(Message m) {
		return msgs.remove(m);
	}

	public Message getlast() {
		return msgs.get(lastMsg - 1);
	}

	public Message getAt(int location) {
		return msgs.get(location);
	}

	public boolean hasMsg(Message m) {
		return msgs.contains(m);
	}

	public int getSize() {
		return lastMsg;
	}

	public String getLastSaid() {
		return msgs.get(lastMsg - 1).getText();
	}

	public Contact getReceiver() {
		return receiver;
	}

	public void setReceiver(Contact receiver) {
		this.receiver = receiver;
	}

	public int getLastMsg() {
		return lastMsg;
	}

	public void setLastMsg(int lastMsg) {
		this.lastMsg = lastMsg;
	}

	public Contact getSender() {
		return sender;
	}

	public void setSender(Contact sender) {
		this.sender = sender;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
