/* *****************************************************************************
 * AnnouncementHandler.java - created by aaronz
 * 
 * Copyright (c) 2006 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu)
 * 
 * ****************************************************************************/

package org.sakaiproject.importer.impl.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementService;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.importer.api.HandlesImportable;
import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.impl.importables.Announcement;
import org.sakaiproject.site.api.SiteService;

/**
 * This handler takes the generic announcement objects created by the various
 * translators and turns them into Sakai announcements in the current course
 * (created by the current user)
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class AnnouncementHandler implements HandlesImportable {

	private static Log log = LogFactory.getLog(AnnouncementHandler.class);

	protected AnnouncementService announcementService = null;
	
	public void setAnnouncementService(AnnouncementService announcementService) {
		this.announcementService = announcementService;
	}


	public boolean canHandleType(String typeName) {
		return typeName.equals("sakai-announcement");
	}

	public void handle(Importable thing, String siteId) {
		if(canHandleType(thing.getTypeName())) {
			// Cast the thing as the right type of item
			Announcement item = (Announcement) thing;

			// should probably spring inject these
//			ComponentManager cm = org.sakaiproject.component.cover.ComponentManager.getInstance();
//			AnnouncementService announcementService = (AnnouncementService) cm.get(AnnouncementService.class);
//			SiteService siteService = (SiteService) cm.get(SiteService.class);

			AnnouncementChannel ac = null;
			try {
				String channelId = announcementService.channelReference(siteId, SiteService.MAIN_CONTAINER);
				ac = announcementService.getAnnouncementChannel(channelId);
			} catch (IdUnusedException e) {
				log.error("Failed to get announcement channel: " + e);
				e.printStackTrace();
				return;
			} catch (PermissionException e) {
				log.error("Failed to get announcement channel: " + e);
				e.printStackTrace();
				return;
			}

			try {
				ac.addAnnouncementMessage(item.getTitle(), false, null, item.getDescription());
			} catch (PermissionException e) {
				log.error("Failed to create announcement message: " + e);
				e.printStackTrace();
				return;
			}
			
			// TODO - Need to handle things like email reminders and other possible optional fields
/***
			try {
				AnnouncementMessageEdit ame = ac.addAnnouncementMessage();
				ame.setBody(item.getDescription());
				ResourcePropertiesEdit rpe = ame.getPropertiesEdit();
				rpe.addProperty(ResourceProperties.PROP_PUBVIEW, Boolean.TRUE.toString());
				AnnouncementMessageHeaderEdit ahe = ame.getAnnouncementHeaderEdit();
				ahe.setSubject(item.getTitle());
				ahe.setDate(new MyTime(item.getCreated().getTime()));
				ac.commitMessage(ame);
			} catch (PermissionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
***/		

			log.info("Created new announcement ("+item.getTitle()+") in " + siteId);
		} else {
			log.warn("Attempted to import object ("+thing.getClass()+") using wrong handler ("+this.getClass()+")");
		}
	}

}
