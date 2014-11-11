/*
 * Copyright 2013 McEvoy Software Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.milton.http.annotated;

import io.milton.annotations.PutChild;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import java.io.IOException;

import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class PutChildAnnotationHandler extends AbstractAnnotationHandler {

	private static final Logger log = LoggerFactory.getLogger(PutChildAnnotationHandler.class);

	public PutChildAnnotationHandler(final AnnotationResourceFactory outer) {
		super(outer, PutChild.class, Method.PUT);
	}

	public Object execute(AnnoResource res, String newName, InputStream inputStream, Long length, String contentType) throws ConflictException, NotAuthorizedException, BadRequestException {
		log.trace("execute PUT method");
		Object source = res.getSource();
		ControllerMethod cm = getBestMethod(source.getClass());
		if (cm == null) {
			if (controllerMethods.isEmpty()) {
				log.info("Method not found for source: {}. No methods registered for {}", source.getClass().getSimpleName(), PutChild.class.getSimpleName());
			} else {
				log.info("Method not found for source {}. Listing methods registered for {}: {}", new Object[]{source.getClass().getSimpleName(), PutChild.class.getSimpleName(), StringUtils.join(controllerMethods, ",")});
			}
			throw new RuntimeException("Method not found: " + getClass() + " - " + source.getClass());
		}
		try {
			//Object[] args = outer.buildInvokeArgs(source, cm.method, newName, inputStream, length, contentType);
			//return cm.method.invoke(cm.controller, args); 
			return invoke(cm, res, newName, inputStream, length, contentType);
		} catch (NotAuthorizedException e) {
			throw e;
		} catch (BadRequestException e) {
			throw e;
		} catch (ConflictException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Object replace(AnnoFileResource fileRes, InputStream inputStream, Long length) throws ConflictException, NotAuthorizedException, BadRequestException {
		log.trace("execute PUT (replace) method");
		Object source = fileRes.getSource();
		ControllerMethod cm = getBestMethod(source.getClass());
		if (cm == null) {
			// ok, cant replace. Maybe we can delete and PUT?
			String name = fileRes.getName();
			annoResourceFactory.deleteAnnotationHandler.execute(fileRes);
			return execute(fileRes.getParent(), name, inputStream, length, null);

		} else {
			try {
                return invoke(cm, fileRes, inputStream, length, fileRes);
			} catch (NotAuthorizedException e) {
				throw e;
			} catch (BadRequestException e) {
				throw e;
			} catch (ConflictException e) {
				throw e;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
