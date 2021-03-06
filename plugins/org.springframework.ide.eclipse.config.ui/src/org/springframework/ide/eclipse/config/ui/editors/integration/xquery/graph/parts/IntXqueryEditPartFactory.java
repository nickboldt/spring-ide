/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.editors.integration.xquery.graph.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.IntegrationImages;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.BadgedIntegrationPart;
import org.springframework.ide.eclipse.config.ui.editors.integration.graph.parts.RouterGraphicalPart;
import org.springframework.ide.eclipse.config.ui.editors.integration.xquery.graph.model.XqueryRouterModelElement;
import org.springframework.ide.eclipse.config.ui.editors.integration.xquery.graph.model.XqueryTransformerModelElement;

public class IntXqueryEditPartFactory implements EditPartFactory {

	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		if (model instanceof XqueryRouterModelElement) {
			part = new RouterGraphicalPart((XqueryRouterModelElement) model, IntegrationImages.ROUTER,
					IntegrationImages.BADGE_SI_XQUERY);
		}
		else if (model instanceof XqueryTransformerModelElement) {
			part = new BadgedIntegrationPart((XqueryTransformerModelElement) model, IntegrationImages.TRANSFORMER,
					IntegrationImages.BADGE_SI_XQUERY);
		}
		return part;
	}
}
