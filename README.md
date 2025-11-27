# Form Wizard

### Plugin Type
Form Element

### Overview
The **Form Wizard Plugin** provides an easy way to break a single long form into several logical steps (pages).  
It includes:

- A dynamic progress indicator (step circles + connecting lines)
- Clear visual state: **previous**, **active**, and **upcoming** steps
- Navigation controls (previous/next)
- Fully customizable UI via FreeMarker templates and CSS
- Highly flexible layout — works with any form field or form section

This plugin helps improve usability for complex forms such as SLF, PBG, AMDAL, and other multi-section submissions commonly used in enterprise/government workflows.

### Plugin Properties
The plugin exposes several configurable properties to control its behavior:

### **Steps Detection**
The plugin automatically detects child elements (pages) under the wizard container:
- Each child with class name matching `${formWizardChildClassName}` is treated as a wizard step.
- The plugin assigns:
  - step number  
  - step label  
  - step state (completed, active, pending)

### **Visual Components**
- **Step Circle** — shows step number  
- **Step Label** — name/description of the step  
- **Connector Line** — visually indicates completion and ordering  

These components are customizable through CSS.

### **Navigation Behavior**
- Clicking a step button navigates directly to that page  
- Next/Previous buttons update the active step  
- Completed steps are marked automatically  

### **Template Customization**
Developers can modify:
- FreeMarker `.ftl` layout for wizard structure  
- CSS classes for theme customization  
- Visibility logic for fields per step  

### **State Management**
The plugin updates:
- `currentPage`
- completed steps list
- validation check (if developer adds validation logic on each page)

### [other detailed documentation such as pre-configuration, assumptions, etc.]

### Version History

*  **yyyymmdd**
   * [Specify list of any changes here]

*  **20251127**
   * Initial creation
   * Migration from Kecak v1
