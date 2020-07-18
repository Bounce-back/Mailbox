package io.github.bounceback.mailbox;

import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class Mailbox extends JavaPlugin {
	//Maps UUID (as a string) to a mailbox
	private HashMap<String,HashMap<MailboxKey,String>> mailboxes=new HashMap<String,HashMap<MailboxKey,String>>();
	
	public void onEnable() {
		getLogger().info("onEnable has been invoked!");
		
		File file=new File("mailboxes.txt");
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			else {
				FileInputStream f = new FileInputStream(file);
				ObjectInputStream o=new ObjectInputStream(f);
				mailboxes=(HashMap<String,HashMap<MailboxKey,String>>) o.readObject();
				o.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public void onDisable() {
		getLogger().info("onDisable has been invoked!");
		updateMailboxes();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender,
			Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("mail")) {
			String userId=Bukkit.getPlayer(sender.getName()).getUniqueId().toString();
			if(!mailboxes.containsKey(userId)) {
				mailboxes.put(userId, new HashMap<MailboxKey,String>());
			}
			if(args==null||args.length==0) {
				sender.sendMessage("Heya! You look a little lost, do come in though. "
						+ "Here, have some cookies I was baking. I was making them just in case someone dropped by... "
						+ "Next time, I think ya wanna try /mail send [playername] [message], "
						+ "/mail inbox {playername/pagenumber} or /mail sent {playername/pagenumber}, "
						+ "unless ya wanna pop in again :)");
			}
			else if(args[0].equals("send")) {
				if(args.length<=2) {
					sender.sendMessage("Format: /mail send [playername] [message]");
				}
				else if(!Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore()) {
					sender.sendMessage("Invalid Name");
				}
				else {
					String otherPlayer=Bukkit.getOfflinePlayer(args[1]).getUniqueId().toString();;
					mailboxes.get(userId).put(new MailboxKey(false,otherPlayer,System.currentTimeMillis()), args[2]);
					if(!mailboxes.containsKey(otherPlayer)) {
						mailboxes.put(otherPlayer, new HashMap<MailboxKey,String>());
					}
					mailboxes.get(otherPlayer).put(new MailboxKey(true,userId,System.currentTimeMillis()), args[2]);
					sender.sendMessage("Sent message: "+args[2]+" to "+args[1]);
					Player otherName=Bukkit.getPlayer(args[1]);
					if(otherName!=null) {
						otherName.sendMessage("You got a message from "+sender.getName()+": "+args[2]);
						updateMailboxes();
					}
				}
			}
			else if(args[0].equals("inbox")||args[0].equals("sent")) {
				boolean isReciever=args[0].equals("inbox");
				if(args.length==1) printMessages(sender,1,isReciever,null);
				else if(args.length>=3) {
					if(!Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore()) {
						sender.sendMessage("Invalid Name");
					}
					else {
						String filteredPlayer=Bukkit.getOfflinePlayer(args[1]).getUniqueId().toString();
						if(isInt(args[2])&&filteredPlayer!=null) {
							printMessages(sender,Integer.parseInt(args[2]),isReciever,filteredPlayer);
						}
					}
				}
				else {
					if(isInt(args[1])) {
						printMessages(sender,Integer.parseInt(args[1]),isReciever,null);
					}
					else if(Bukkit.getOfflinePlayer(args[1]).hasPlayedBefore()) {
						printMessages(sender,1,isReciever,Bukkit.getOfflinePlayer(args[1]).getUniqueId().toString());
					}
				}
			}
			return true;
		}
		return false;
	}
	
	private boolean isInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private void printMessages(CommandSender sender, int pageNum, boolean isReciever, String otherPlayer) {
		String userId=Bukkit.getPlayer(sender.getName()).getUniqueId().toString();
		HashMap<MailboxKey, String> senderMailbox=mailboxes.get(userId);
		int numberMessages=0;
		for(MailboxKey key:senderMailbox.keySet()) {
			if(key.getIsReciever()==isReciever&&
					(otherPlayer==null||key.getOtherPerson().equals(otherPlayer))&&
					numberMessages<pageNum*10) {
				if(pageNum*10-numberMessages<=10) {
					sender.sendMessage(key.toString()+": "+senderMailbox.get(key));
				}
				numberMessages+=1;
			}
		}
		sender.sendMessage("Page "+String.valueOf(pageNum)+" out of "+String.valueOf(numberMessages));
		return;
	}
	
	public void updateMailboxes() {
		File file=new File("mailboxes.txt");
		try {
			if(!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fo=new FileOutputStream(file);
			ObjectOutputStream out=new ObjectOutputStream(fo);
			out.writeObject(mailboxes);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	 }
}
