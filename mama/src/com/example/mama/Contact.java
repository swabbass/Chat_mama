package com.example.mama;

public class Contact {
	private String ID;
	private String name;
	private String number;
	private String email;

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stu
		Contact c;
		if (o instanceof Contact)
			c = (Contact) o;
		else
			return false;
		return this.name.equals(c.getName());
	}

	public Contact(String ID, String name, String number, String email) {
		setEmail(email);
		setID(ID);
		setName(name);
		setNumber(number);
		// TODO Auto-generated constructor stub
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		if (iD != null)
			ID = iD;
		else
			ID = "";
	}

	public String getName() {
		return name;
	}
	public String getSQLName() {
		char c = 34;
		Character ch = Character.valueOf(c);
		return ch.toString()+name+ch.toString();
	}
	public void setName(String name) {
		if (name == null)
			this.name = "";
		else
			this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		if (number == null)
			this.number = "";
		else
			this.number = number;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		if (email == null)
			email = "";
		else
			this.email = email;
	}

}
