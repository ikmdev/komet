## What is the Concept Detail View? 
This module is based on Amplify by Design's prototype and wireframe. The derived work is from Komet's existing concept details view. The main differences are the following:
1. The prior (existing) `Details` Tab displays all versions (STAMPs) of a **concept** and a **semantic**. The new design shows the **latest** concept chronology.
2. The UI/UX is styled based on a newer look.

As you can see below is the **Concept Detail View** of a specific concept when a user selects a concept from the **Concept Navigator Panel** (Tab located on the left hand side) in Komet application.
![concept-detail-view.png](docs%2Fconcept-detail-view.png)

The newer concept detail view has three sections as follows:
1. **Latest Concept** Version display (Banner)
   - Identicon
   - Definition
   - Identifier
   - STAMP information
2. **Descriptions** - The concept's fully qualified name and other names (synonyms)
   - Pattern field definitions such as case significants and language description
   - An image to illustrate the concept
3. **Axioms**
   - Inferred or Implied axioms (machine based classifiers)
   - Stated axioms (human based expressions)


**Note:** In the upper left beside the concept name is an [identicon](https://github.com/bryc/code/wiki/Identicons) . Identicons are used to assist the user to easily view a unique concept's UUID instead of having to read a long hash value. 

## Getting started Concept Details View
As a user of the Komet application by default the UI will display the **Concept Navigator** panel typically on the left split pane area. Once the user selects a concept in the **Concept Navigator** panel the **Concept Detail View** panel will get populated. 

Of course you'll have to first open a Concept Details View panel (subscribed to the **Concept Navigator** panel)

**Step 1.** In the center (split pane) select the **'+'** drop down menu to select `Amplify Details` option.
![amplify-details-menu-option.png](docs%2Famplify-details-menu-option.png)

**Step 2.** Select with **navigation stream subscription** option.
![amplify-details-menu-option-navigator-subscribe.png](docs%2Famplify-details-menu-option-navigator-subscribe.png)

**Step 3.** Select a concept from the Concept Navigator panel (left split pane area)


# Developer Notes:


This module will provide the latest concept details displayed when the user selects a concept inside the Concept Navigator View.
When developing code and building you will need to build `tinkar-core` then `komet`. 
For a faster development workflow when changes are made in this module simply build just this module as shown below:
```shell
 mvn -f amplifydetails clean install
```

The following are two CSS files that offer two Look & Feel based on Amplify by Design's prototypes.
1. [amplify-details-opt-2.css](src%2Fmain%2Fresources%2Fdev%2Fikm%2Fkomet%2Famplifydetails%2Famplify-details-opt-2.css)
2. [amplify-details-opt-1a.css](src%2Fmain%2Fresources%2Fdev%2Fikm%2Fkomet%2Famplifydetails%2Famplify-details-opt-1a.css)

To switch between them the code would need to be changed inside [AmplifyDetailsNode.java](src%2Fmain%2Fjava%2Fdev%2Fikm%2Fkomet%2Famplifydetails%2FAmplifyDetailsNode.java) of the `init()` method.

### Outstanding items:
- Image of a concept should allow user to choose a picture.
- Resizing issues still occur when concept names are extremely long.