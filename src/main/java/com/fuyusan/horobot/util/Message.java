/*
	HoroBot - An open-source Discord bot
	Copyright (C) 2017	WiNteR

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.fuyusan.horobot.util;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.MessageFormat;

public class Message {

	public static void sendFile(IChannel channel, String message, File file) {
		if(channel.getModifiedPermissions(channel.getClient().getOurUser().getRolesForGuild(channel.getGuild()).get(0)).contains(Permissions.SEND_MESSAGES)) {
			RequestBuffer.request(() -> {
				try {
					channel.sendFile(message, file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			});
		}
	}

	public static void sendFile(IChannel channel, String message, String name, InputStream file) {
		RequestBuffer.request(() -> {
			channel.sendFile(message, false, file, name);
		});
	}

	public static void sendFile(IChannel channel, EmbedObject embed, String message, String name, InputStream file) {
		RequestBuffer.request(() -> {
			channel.sendMessage(message);
			channel.sendFile(embed, file, name);
		});
	}

	public static void sendEmbed(IChannel channel, String message, EmbedObject embed, boolean inline) {
		RequestBuffer.request(() -> {
			try {
				channel.sendMessage(message, embed, inline);
			} catch (Exception e) {
				if(e instanceof RateLimitException) {
					throw e;
				} else {
					channel.sendMessage("No results");
				}
			}
		});
	}

	public static void reply(String str, IMessage message) {
		RequestBuffer.request(() -> {
			message.reply(Localisation.getMessage(message.getGuild().getID(), str));
		});
	}

	public static void replyRaw(String str, IMessage message) {
		RequestBuffer.request(() -> {
			message.reply(str);
		});
	}

	public static void sendMessageInChannel(IChannel channel, String message, Object... args) {
		RequestBuffer.request(() -> {
			channel.sendMessage(String.format(Localisation.getMessage(channel.getGuild().getID(), message), args));
		});
	}

	public static void sendRawMessageInChannel(IChannel channel, String message) {
		RequestBuffer.request(() -> {
			channel.sendMessage(message);
		});
	}

	public static void sendPM(String message, IUser user) {
		RequestBuffer.request(() -> {
			user.getOrCreatePMChannel().sendMessage(Localisation.getPMMessage(message));
		});
	}

	public static void sendRawPM(String message, IUser user) {
		RequestBuffer.request(() -> {
			user.getOrCreatePMChannel().sendMessage(message);
		});
	}
}