package io.github.bounceback.mailbox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.bukkit.Bukkit;

public class MailboxKey implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	private boolean isReciever;
	private String otherPerson; //UUID as a string
	private long timestamp;
	public MailboxKey(boolean isReciever, String otherPerson, long timestamp) {
		this.isReciever=isReciever;
		this.otherPerson=otherPerson;
		this.timestamp=timestamp;
	}
	
	public boolean getIsReciever() {
		return isReciever;
	}
	
	public String getOtherPerson() {
		return otherPerson;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public String toString() {
		try {
		String direction=isReciever?"From ":"To ";
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMddHHmmss");
		Date date=dateFormat.parse(String.valueOf(timestamp));
		SimpleDateFormat dateFormatNew = new SimpleDateFormat("dd/MM/yyyy");
		
		return direction+Bukkit.getPlayer(UUID.fromString(otherPerson)).getName()+" on "+
				dateFormatNew.format(date);
		} catch (Exception e) {
			e.getStackTrace();
		}
		return "error";
	}
}
